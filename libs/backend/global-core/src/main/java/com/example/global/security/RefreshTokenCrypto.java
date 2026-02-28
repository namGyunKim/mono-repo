package com.example.global.security;

import com.example.global.security.jwt.JwtProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class RefreshTokenCrypto {

    private static final String KDF_ALGORITHM = "HKDF-SHA256";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String AES_ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final int KEY_LENGTH_BYTES = 32;
    private static final byte[] KEY_DERIVATION_SALT = "gyun-refresh-token".getBytes(StandardCharsets.UTF_8);
    private static final byte[] KEY_DERIVATION_INFO = "refresh-token-aes-gcm-key".getBytes(StandardCharsets.UTF_8);

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenCrypto(final JwtProperties jwtProperties) {
        this.secretKey = new SecretKeySpec(deriveKeyMaterial(jwtProperties.secret()), AES_ALGORITHM);
    }

    private static byte[] deriveKeyMaterial(final String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("리프레시 토큰 암호화 키 시드(secret)가 비어 있습니다.");
        }

        try {
            final HKDFParameterSpec parameters = HKDFParameterSpec.ofExtract()
                    .addIKM(secret.getBytes(StandardCharsets.UTF_8))
                    .addSalt(KEY_DERIVATION_SALT)
                    .thenExpand(KEY_DERIVATION_INFO, KEY_LENGTH_BYTES);

            final KDF kdf = KDF.getInstance(KDF_ALGORITHM);
            return kdf.deriveData(parameters);
        } catch (final NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("리프레시 토큰 암호화 키 생성에 실패했습니다.", e);
        }
    }

    public String encrypt(final String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return "";
        }

        try {
            final byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] cipherText = cipher.doFinal(refreshToken.getBytes(StandardCharsets.UTF_8));

            final ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (final Exception e) {
            throw new IllegalStateException("리프레시 토큰 암호화에 실패했습니다.", e);
        }
    }

    public String decrypt(final String encryptedRefreshToken) {
        if (!StringUtils.hasText(encryptedRefreshToken)) {
            return "";
        }

        try {
            final byte[] combined = Base64.getDecoder().decode(encryptedRefreshToken);
            if (combined.length <= IV_LENGTH) {
                throw new IllegalStateException("리프레시 토큰 암호문 형식이 올바르지 않습니다.");
            }

            final byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            final byte[] cipherText = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (final IllegalStateException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException("리프레시 토큰 복호화에 실패했습니다.", e);
        }
    }
}
