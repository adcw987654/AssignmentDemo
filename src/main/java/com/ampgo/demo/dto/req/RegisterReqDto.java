package com.ampgo.demo.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterReqDto {

    @NotBlank(message = "Account cannot be blank")
    @Email(message = "Account must be a valid email address")
    private String account;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}