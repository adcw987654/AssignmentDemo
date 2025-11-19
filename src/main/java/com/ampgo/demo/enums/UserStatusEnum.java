package com.ampgo.demo.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

    ACTIVE(1),
    INACTIVE(0);

    private final int code;

    UserStatusEnum(int code) {
        this.code = code;
    }
}
