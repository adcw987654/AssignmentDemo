package com.ampgo.demo.dto.res;

import lombok.Data;

@Data
public class ResponseDto<T> {
    private String code;
    private String message;
    private T data;

    public static <Type> ResponseDto<Type> successRes(Type data) {
        ResponseDto<Type> res = new ResponseDto<>();
        res.data = data;
        res.code = "200";
        res.message = "success";
        return res;
    }

    public static ResponseDto failedRes(String message) {
        ResponseDto res = new ResponseDto<>();
        res.code = "500";
        res.message = message;
        return res;
    }

    public static <Type> ResponseDto<Type> failedRes(String message, Type data) {
        ResponseDto<Type> res = new ResponseDto<>();
        res.code = "400";
        res.message = message;
        res.data = data;
        return res;
    }
}