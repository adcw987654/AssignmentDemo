package com.ampgo.demo.controller;

import com.ampgo.demo.dto.req.LoginReqDto;
import com.ampgo.demo.dto.req.RegisterReqDto;
import com.ampgo.demo.dto.req.VerifyEmailReqDto;
import com.ampgo.demo.dto.res.LoginResDto;
import com.ampgo.demo.dto.res.ProfileResDto;
import com.ampgo.demo.dto.res.ResponseDto;
import com.ampgo.demo.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseDto<LoginResDto> login(@Valid @RequestBody LoginReqDto reqDto) {
        LoginResDto resDto = loginService.login(reqDto);
        return resDto == null
                ? ResponseDto.failedRes("account / password error!")
                : ResponseDto.successRes(resDto);
    }

    @PostMapping("/register")
    public ResponseDto<Void> register(@Valid @RequestBody RegisterReqDto reqDto) {
        loginService.register(reqDto);
        return ResponseDto.successRes(null);
    }

    @PostMapping("/verify-email")
    public ResponseDto<Void> verifyEmail(@Valid @RequestBody VerifyEmailReqDto reqDto) {
        loginService.verifyEmail(reqDto);
        return ResponseDto.successRes(null);
    }

    @GetMapping("/profile")
    public ResponseDto<ProfileResDto> getProfile(@RequestHeader(name = "Authorization") String token) {

        ProfileResDto resDto = loginService.getProfile(token);

        return resDto == null
                ? ResponseDto.failedRes("user not found!")
                : ResponseDto.successRes(resDto);
    }
}
