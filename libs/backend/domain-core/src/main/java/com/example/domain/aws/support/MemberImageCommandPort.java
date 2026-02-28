package com.example.domain.aws.support;

/**
 * AWS 도메인이 회원 이미지 등록/삭제를 요청하는 Port
 *
 * <p>
 * - Port 정의: aws/support (사용하는 도메인)
 * - Adapter 구현: member/support (제공하는 도메인)
 * - primitive 파라미터로 도메인 간 결합도를 최소화
 * </p>
 */
public interface MemberImageCommandPort {

    Long registerProfileImage(Long memberId, String uploadDirect, String fileName);

    void deleteProfileImage(Long memberImageId);
}
