package com.ampgo.demo.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VerificationCodeEntity {

    private Long id;
    private String email;
    private String code;
    private LocalDateTime expirationTime;
    private Boolean verified;
    private LocalDateTime createTime;
}
