package com.ampgo.demo.service;

import com.ampgo.demo.dto.req.LoginReqDto;
import com.ampgo.demo.dto.req.RegisterReqDto;
import com.ampgo.demo.dto.req.VerifyEmailReqDto;
import com.ampgo.demo.dto.res.LoginResDto;
import com.ampgo.demo.dto.res.ProfileResDto;

public interface LoginService {
    LoginResDto login(LoginReqDto reqDto);

    void register(RegisterReqDto reqDto);

    void verifyEmail(VerifyEmailReqDto reqDto);

    ProfileResDto getProfile(String userId);
}
