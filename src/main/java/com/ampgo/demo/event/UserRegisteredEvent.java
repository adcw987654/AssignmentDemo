package com.ampgo.demo.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户注册事件
 * 当用户成功注册时发布此事件
 */
@Getter
@AllArgsConstructor
public class UserRegisteredEvent {

    private final String email;
    private final String verificationCode;
}
