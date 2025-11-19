package com.ampgo.demo.service.impl;

import com.ampgo.demo.bo.UserAccountBo;
import com.ampgo.demo.dto.req.LoginReqDto;
import com.ampgo.demo.dto.req.RegisterReqDto;
import com.ampgo.demo.dto.req.VerifyEmailReqDto;
import com.ampgo.demo.dto.res.LoginResDto;
import com.ampgo.demo.dto.res.ProfileResDto;
import com.ampgo.demo.entity.UserEntity;
import com.ampgo.demo.entity.VerificationCodeEntity;
import com.ampgo.demo.enums.UserStatusEnum;
import com.ampgo.demo.event.UserRegisteredEvent;
import com.ampgo.demo.exception.UserValidException;
import com.ampgo.demo.mapper.UserMapper;
import com.ampgo.demo.mapper.VerificationCodeMapper;
import com.ampgo.demo.service.LoginService;
import com.ampgo.demo.service.UserValidService;
import com.ampgo.demo.utils.EncryptUtil;
import com.ampgo.demo.utils.JwtUtil;
import com.ampgo.demo.utils.VerificationCodeUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final UserMapper userMapper;
    private final UserValidService userValidService;
    private final VerificationCodeMapper verificationCodeMapper;
    private final ApplicationEventPublisher eventPublisher;
    private static final int CODE_EXPIRATION_MINUTES = 30;
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterReqDto reqDto) {
        if (userValidService.isUserExist(
                UserAccountBo.builder()
                        .account(reqDto.getAccount())
                        .build())) {
            log.error("account exist!");
            throw new UserValidException("account already exists!");
        }
        UserEntity user = convertEntity(reqDto);
        userMapper.insert(user);
        // generate and send verification code
        String verificationCode = VerificationCodeUtil.generateVerificationCode();
        VerificationCodeEntity codeEntity = convertVerificationCodeEntity(reqDto.getAccount(), verificationCode);
        verificationCodeMapper.insert(codeEntity);
        log.info("Publishing user registered event for email: {}", reqDto.getAccount());
        eventPublisher.publishEvent(new UserRegisteredEvent(reqDto.getAccount(), verificationCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(VerifyEmailReqDto reqDto) {
        VerificationCodeEntity codeEntity = verificationCodeMapper.findByEmailAndCode(
                reqDto.getEmail(), reqDto.getCode());
        if (codeEntity == null) {
            log.error("Invalid verification code for email: {}", reqDto.getEmail());
            throw new UserValidException("Invalid verification code!");
        }
        if (LocalDateTime.now().isAfter(codeEntity.getExpirationTime())) {
            log.error("Verification code expired for email: {}", reqDto.getEmail());
            throw new UserValidException("Verification code has expired!");
        }
        // Mark code as verified
        verificationCodeMapper.updateVerified(codeEntity.getId());
        // Update user email verified status
        userMapper.updateVerifyStatus(reqDto.getEmail(), UserStatusEnum.ACTIVE.getCode());
    }

    @Override
    public LoginResDto login(LoginReqDto reqDto) {
        return userMapper.findByAccount(reqDto.getAccount())
                .map(user -> {
                    if (UserStatusEnum.INACTIVE.getCode() == user.getStatus()) {
                        throw new UserValidException("Email not verified!");
                    }
                    if (!EncryptUtil.isMatchPassword(reqDto.getPassword(), user.getPassword())) {
                        log.warn("password not correct!, userId:{}", user.getId());
                        return null;
                    }
                    userMapper.updateLastLoginTime(user.getId());
                    LoginResDto resDto = new LoginResDto();
                    resDto.setAccessToken(JwtUtil.generateToken(user.getId(), user.getAccount(), jwtSecret));
                    return resDto;
                })
                .orElse(null);
    }

    @Override
    public ProfileResDto getProfile(String token) {

        Claims jwtClaims = JwtUtil.parseToken(token, jwtSecret);

        return userMapper.findById(jwtClaims.get("userId", Long.class))
                .map(this::convertToLoginResDto)
                .orElseGet(() -> {
                    log.error("user not found!");
                    return null;
                });
    }

    public UserEntity convertEntity(RegisterReqDto reqDto) {
        UserEntity userEntity = new UserEntity();
        userEntity.setAccount(reqDto.getAccount());
        userEntity.setPassword(EncryptUtil.bcryptEncoder(reqDto.getPassword()));
        userEntity.setStatus(UserStatusEnum.INACTIVE.getCode());
        return userEntity;
    }

    public ProfileResDto convertToLoginResDto(UserEntity user) {
        ProfileResDto profileResDto = new ProfileResDto();
        profileResDto.setId(user.getId());
        profileResDto.setAccount(user.getAccount());
        profileResDto.setLastLoginTime(
                user.getLastLoginTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return profileResDto;
    }

    private VerificationCodeEntity convertVerificationCodeEntity(String account, String verificationCode) {
        VerificationCodeEntity codeEntity = new VerificationCodeEntity();
        codeEntity.setEmail(account);
        codeEntity.setCode(verificationCode);
        codeEntity.setExpirationTime(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        codeEntity.setVerified(false);
        codeEntity.setCreateTime(LocalDateTime.now());
        return codeEntity;
    }
}
