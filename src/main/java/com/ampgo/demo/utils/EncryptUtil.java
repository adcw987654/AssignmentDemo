package com.ampgo.demo.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

public class EncryptUtil {

    private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(6, new SecureRandom());

    public static String bcryptEncoder(String bcrypt) {
        return encoder.encode(bcrypt);
    }

    public static Boolean isMatchPassword(String password, String encryptPassword) {
        return encoder.matches(password, encryptPassword);
    }
}
