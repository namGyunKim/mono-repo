/**
 * 이 파일은 자동 생성됩니다 — 직접 수정하지 마세요.
 * 생성 명령: ./gradlew :libs:backend:domain-core:generateContractEnumTs
 * 원본: libs/backend/domain-core/.../contract/enums/
 */

export enum ApiAccountRole {
    SUPER_ADMIN = 'SUPER_ADMIN',
    ADMIN = 'ADMIN',
    USER = 'USER',
    GUEST = 'GUEST',
}

export enum ApiLogType {
    JOIN = 'JOIN',
    LOGIN = 'LOGIN',
    LOGIN_FAIL = 'LOGIN_FAIL',
    LOGOUT = 'LOGOUT',
    EXCEPTION = 'EXCEPTION',
    UPDATE = 'UPDATE',
    INACTIVE = 'INACTIVE',
    PASSWORD_CHANGE = 'PASSWORD_CHANGE',
}

export enum ApiMemberActiveStatus {
    ALL = 'ALL',
    ACTIVE = 'ACTIVE',
    INACTIVE = 'INACTIVE',
}

export enum ApiMemberFilterType {
    ALL = 'ALL',
    NICK_NAME = 'NICK_NAME',
    LOGIN_ID = 'LOGIN_ID',
}

export enum ApiMemberOrderType {
    CREATE_ASC = 'CREATE_ASC',
    CREATE_DESC = 'CREATE_DESC',
}

export enum ApiMemberType {
    GENERAL = 'GENERAL',
    GOOGLE = 'GOOGLE',
    GUEST = 'GUEST',
    ALL = 'ALL',
}

