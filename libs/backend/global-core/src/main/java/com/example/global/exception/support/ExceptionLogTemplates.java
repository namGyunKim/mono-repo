package com.example.global.exception.support;

public final class ExceptionLogTemplates {

    public static final String TYPE_MISMATCH_LOG_TEMPLATE = """
            [EXCEPTION]
            traceId={}
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            name={}
            value={}
            """;

    public static final String MISSING_PARAMETER_LOG_TEMPLATE = """
            [EXCEPTION]
            traceId={}
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            name={}
            type={}
            """;

    public static final String MESSAGE_ONLY_LOG_TEMPLATE = """
            [EXCEPTION]
            traceId={}
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            message={}
            """;

    public static final String ACCESS_DENIED_LOG_TEMPLATE = """
            [ACCESS_DENIED]
            traceId={}
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            message={}
            """;

    public static final String UNEXPECTED_EXCEPTION_LOG_TEMPLATE = """
            [EXCEPTION]
            traceId={}
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            accountId={}
            loginId={}
            """;

    public static final String EXCEPTION_EVENT_LOG_TEMPLATE = """
            [EXCEPTION_EVENT]
            traceId={}
            txStatus={}
            exception={}
            """;

    public static final String EXCEPTION_EVENT_STRUCTURED_LOG_TEMPLATE = """
            [EXCEPTION_EVENT_STRUCTURED]
            traceId={}
            txStatus={}
            payload={}
            """;

    public static final String EXCEPTION_EVENT_STRUCTURED_FAIL_LOG_TEMPLATE = """
            [EXCEPTION_EVENT_STRUCTURED_FAIL]
            traceId={}
            txStatus={}
            errorCode={}
            exceptionName={}
            message={}
            """;

    public static final String FILTER_LOG_TEMPLATE = """
            [FILTER]
            traceId={}
            ip={}
            loginId={}
            method={}
            uri={}
            status={}
            time={}ms
            """;

    public static final String FILTER_EXCEPTION_LOG_TEMPLATE = """
            [FILTER]
            traceId={}
            ip={}
            loginId={}
            method={}
            uri={}
            status={}
            time={}ms
            errorName={}
            errorCode={}
            errorMessage={}
            exception={}
            message={}
            """;

    public static final String AUTHENTICATION_ENTRYPOINT_LOG_TEMPLATE = """
            [AUTHENTICATION_REQUIRED]
            traceId={}
            ip={}
            method={}
            uri={}
            errorName={}
            errorCode={}
            errorMessage={}
            message={}
            """;

    public static final String LOGIN_MISSING_CREDENTIAL_LOG_TEMPLATE = """
            [LOGIN_MISSING_CREDENTIAL]
            traceId={}
            ip={}
            method={}
            uri={}
            loginId={}
            errors={}
            """;

    public static final String LOGIN_AUTH_FAILURE_LOG_TEMPLATE = """
            [LOGIN_AUTH_FAILURE]
            traceId={}
            ip={}
            method={}
            uri={}
            loginId={}
            reason={}
            """;

    public static final String LOGIN_JSON_BAD_REQUEST_LOG_TEMPLATE = """
            [LOGIN_JSON_BAD_REQUEST]
            traceId={}
            ip={}
            method={}
            uri={}
            loginId={}
            errorName={}
            errorCode={}
            errorMessage={}
            errors={}
            """;

    public static final String LOG_EVENT_TEMPLATE = """
            [LOG_EVENT]
            traceId={}
            txStatus={}
            message={}
            """;

    private ExceptionLogTemplates() {
    }
}
