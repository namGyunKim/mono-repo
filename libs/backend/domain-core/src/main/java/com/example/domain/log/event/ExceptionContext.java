package com.example.domain.log.event;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.exception.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;

/**
 * ExceptionEvent 생성에 필요한 컨텍스트 정보를 묶는 DTO
 *
 * <p>
 * ExceptionEvent.from() 파라미터 수를 줄이기 위한 전용 record
 * </p>
 */
public record ExceptionContext(
        Exception exception,
        ErrorCode errorCode,
        String errorDetailMsg,
        CurrentAccountDTO account,
        HttpServletRequest httpServletRequest
) {

    public static ExceptionContext of(
            Exception exception,
            ErrorCode errorCode,
            String errorDetailMsg,
            CurrentAccountDTO account,
            HttpServletRequest httpServletRequest
    ) {
        return new ExceptionContext(exception, errorCode, errorDetailMsg, account, httpServletRequest);
    }

    public static ExceptionContext of(
            Exception exception,
            CurrentAccountDTO account,
            HttpServletRequest httpServletRequest
    ) {
        return new ExceptionContext(exception, null, null, account, httpServletRequest);
    }
}
