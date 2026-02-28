package com.example.global.annotation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 현재 로그인한 유저의 정보를 가져오기 위한 어노테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Schema(hidden = true)
@Documented
public @interface CurrentAccount {
}
