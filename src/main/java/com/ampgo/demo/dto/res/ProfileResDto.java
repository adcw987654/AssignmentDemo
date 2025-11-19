package com.ampgo.demo.dto.res;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResDto {

    private Long id;
    private String account;
    private Long lastLoginTime;
}
