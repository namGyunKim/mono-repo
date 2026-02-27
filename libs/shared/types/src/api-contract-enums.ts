/**
 * API contract enums shared between projects.
 * Values are intentionally aligned with backend contract enum names.
 */
export enum ApiAccountRole {
    SUPER_ADMIN = 'SUPER_ADMIN',
    ADMIN = 'ADMIN',
    USER = 'USER',
    GUEST = 'GUEST',
}

export enum ApiMemberType {
    GENERAL = 'GENERAL',
    GOOGLE = 'GOOGLE',
    GUEST = 'GUEST',
    ALL = 'ALL',
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
