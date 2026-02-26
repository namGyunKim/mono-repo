package com.example.domain.log.entity;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class MemberLogId implements Serializable {

    private Long id;
    private LocalDateTime createdAt;
}
