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
import com.ampgo.demo.service.UserValidService;
import com.ampgo.demo.utils.EncryptUtil;
import com.ampgo.demo.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserValidService userValidService;
    @Mock
    private VerificationCodeMapper verificationCodeMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private LoginServiceImpl loginService;
    private static final String JWT_SECRET = "test-secret-key-for-jwt-token-generation";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginService, "jwtSecret", JWT_SECRET);
    }

    @Test
    void testRegister_Success() {
        RegisterReqDto reqDto = new RegisterReqDto();
        reqDto.setAccount("test@example.com");
        reqDto.setPassword("password123");
        when(userValidService.isUserExist(any(UserAccountBo.class))).thenReturn(false);
        loginService.register(reqDto);
        verify(userMapper, times(1)).insert(any(UserEntity.class));
        verify(verificationCodeMapper, times(1)).insert(any(VerificationCodeEntity.class));
        verify(eventPublisher, times(1)).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        RegisterReqDto reqDto = new RegisterReqDto();
        reqDto.setAccount("test@example.com");
        reqDto.setPassword("password123");
        when(userValidService.isUserExist(any(UserAccountBo.class))).thenReturn(true);
        UserValidException exception = assertThrows(UserValidException.class, () -> {
            loginService.register(reqDto);
        });
        assertEquals("account already exists!", exception.getMessage());
        verify(userMapper, never()).insert(any(UserEntity.class));
        verify(verificationCodeMapper, never()).insert(any(VerificationCodeEntity.class));
    }

    @Test
    void testRegister_VerificationCodeGeneration() {
        RegisterReqDto reqDto = new RegisterReqDto();
        reqDto.setAccount("test@example.com");
        reqDto.setPassword("password123");
        when(userValidService.isUserExist(any(UserAccountBo.class))).thenReturn(false);
        ArgumentCaptor<VerificationCodeEntity> codeCaptor = ArgumentCaptor.forClass(VerificationCodeEntity.class);
        loginService.register(reqDto);
        verify(verificationCodeMapper).insert(codeCaptor.capture());
        VerificationCodeEntity capturedCode = codeCaptor.getValue();
        assertEquals("test@example.com", capturedCode.getEmail());
        assertNotNull(capturedCode.getCode());
        assertEquals(10, capturedCode.getCode().length());
        assertFalse(capturedCode.getVerified());
        assertNotNull(capturedCode.getExpirationTime());
    }

    @Test
    void testVerifyEmail_Success() {
        VerifyEmailReqDto reqDto = new VerifyEmailReqDto();
        reqDto.setEmail("test@example.com");
        reqDto.setCode("123456");
        VerificationCodeEntity codeEntity = new VerificationCodeEntity();
        codeEntity.setId(1L);
        codeEntity.setEmail("test@example.com");
        codeEntity.setCode("123456");
        codeEntity.setExpirationTime(LocalDateTime.now().plusMinutes(10));
        codeEntity.setVerified(false);
        when(verificationCodeMapper.findByEmailAndCode("test@example.com", "123456"))
                .thenReturn(codeEntity);
        loginService.verifyEmail(reqDto);
        verify(verificationCodeMapper).updateVerified(1L);
        verify(userMapper).updateVerifyStatus("test@example.com", UserStatusEnum.ACTIVE.getCode());
    }

    @Test
    void testVerifyEmail_InvalidCode() {
        VerifyEmailReqDto reqDto = new VerifyEmailReqDto();
        reqDto.setEmail("test@example.com");
        reqDto.setCode("123456");
        when(verificationCodeMapper.findByEmailAndCode("test@example.com", "123456"))
                .thenReturn(null);
        UserValidException exception = assertThrows(UserValidException.class, () -> {
            loginService.verifyEmail(reqDto);
        });
        assertEquals("Invalid verification code!", exception.getMessage());
        verify(verificationCodeMapper, never()).updateVerified(anyLong());
        verify(userMapper, never()).updateVerifyStatus(anyString(), anyInt());
    }

    @Test
    void testVerifyEmail_ExpiredCode() {
        VerifyEmailReqDto reqDto = new VerifyEmailReqDto();
        reqDto.setEmail("test@example.com");
        reqDto.setCode("123456");
        VerificationCodeEntity codeEntity = new VerificationCodeEntity();
        codeEntity.setId(1L);
        codeEntity.setEmail("test@example.com");
        codeEntity.setCode("123456");
        codeEntity.setExpirationTime(LocalDateTime.now().minusMinutes(10));
        codeEntity.setVerified(false);
        when(verificationCodeMapper.findByEmailAndCode("test@example.com", "123456"))
                .thenReturn(codeEntity);
        UserValidException exception = assertThrows(UserValidException.class, () -> {
            loginService.verifyEmail(reqDto);
        });
        assertEquals("Verification code has expired!", exception.getMessage());
        verify(verificationCodeMapper, never()).updateVerified(anyLong());
        verify(userMapper, never()).updateVerifyStatus(anyString(), anyInt());
    }

    @Test
    void testLogin_Success() {
        LoginReqDto reqDto = new LoginReqDto();
        reqDto.setAccount("test@example.com");
        reqDto.setPassword("password123");
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setAccount("test@example.com");
        user.setPassword(EncryptUtil.bcryptEncoder("password123"));
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        when(userMapper.findByAccount("test@example.com")).thenReturn(Optional.of(user));
        try (MockedStatic<EncryptUtil> encryptUtilMock = mockStatic(EncryptUtil.class);
             MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            encryptUtilMock.when(() -> EncryptUtil.isMatchPassword("password123", user.getPassword()))
                    .thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.generateToken(1L, "test@example.com", JWT_SECRET))
                    .thenReturn("test-jwt-token");
            LoginResDto result = loginService.login(reqDto);
            assertNotNull(result);
            assertEquals("test-jwt-token", result.getAccessToken());
            verify(userMapper).updateLastLoginTime(1L);
        }
    }

    @Test
    void testLogin_EmailNotVerified() {
        LoginReqDto reqDto = new LoginReqDto();
        reqDto.setAccount("test@example.com");
        reqDto.setPassword("password123");
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setAccount("test@example.com");
        user.setPassword("hashed-password");
        user.setStatus(UserStatusEnum.INACTIVE.getCode());
        when(userMapper.findByAccount("test@example.com")).thenReturn(Optional.of(user));
        UserValidException exception = assertThrows(UserValidException.class, () -> {
            loginService.login(reqDto);
        });
        assertEquals("Email not verified!", exception.getMessage());
        verify(userMapper, never()).updateLastLoginTime(anyLong());
    }

    @Test
    void testLogin_WrongPassword() {
        LoginReqDto reqDto = new LoginReqDto();
        reqDto.setAccount("test@example.com");
        reqDto.setPassword("wrongpassword");
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setAccount("test@example.com");
        user.setPassword(EncryptUtil.bcryptEncoder("password123"));
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        when(userMapper.findByAccount("test@example.com")).thenReturn(Optional.of(user));
        try (MockedStatic<EncryptUtil> encryptUtilMock = mockStatic(EncryptUtil.class)) {
            encryptUtilMock.when(() -> EncryptUtil.isMatchPassword("wrongpassword", user.getPassword()))
                    .thenReturn(false);
            LoginResDto result = loginService.login(reqDto);
            assertNull(result);
            verify(userMapper, never()).updateLastLoginTime(anyLong());
        }
    }

    @Test
    void testLogin_UserNotFound() {
        LoginReqDto reqDto = new LoginReqDto();
        reqDto.setAccount("nonexistent@example.com");
        reqDto.setPassword("password123");
        when(userMapper.findByAccount("nonexistent@example.com")).thenReturn(Optional.empty());
        LoginResDto result = loginService.login(reqDto);
        assertNull(result);
        verify(userMapper, never()).updateLastLoginTime(anyLong());
    }

    @Test
    void testGetProfile_Success() {
        String token = "Bearer test-jwt-token";
        Claims claims = new DefaultClaims();
        claims.put("userId", 1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setAccount("test@example.com");
        user.setLastLoginTime(LocalDateTime.of(2025, 11, 19, 10, 0));
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.parseToken(token, JWT_SECRET))
                    .thenReturn(claims);
            when(userMapper.findById(1L)).thenReturn(Optional.of(user));
            ProfileResDto result = loginService.getProfile(token);
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("test@example.com", result.getAccount());
            assertNotNull(result.getLastLoginTime());
        }
    }

    @Test
    void testGetProfile_UserNotFound() {
        String token = "Bearer test-jwt-token";
        Claims claims = new DefaultClaims();
        claims.put("userId", 1L);
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.parseToken(token, JWT_SECRET))
                    .thenReturn(claims);
            when(userMapper.findById(1L)).thenReturn(Optional.empty());
            ProfileResDto result = loginService.getProfile(token);
            assertNull(result);
        }
    }

    @Test
    void testConvertEntity() {
        RegisterReqDto reqDto = new RegisterReqDto();
        reqDto.setAccount("test@example.com");
        reqDto.setPassword("password123");
        try (MockedStatic<EncryptUtil> encryptUtilMock = mockStatic(EncryptUtil.class)) {
            encryptUtilMock.when(() -> EncryptUtil.bcryptEncoder("password123"))
                    .thenReturn("hashed-password");
            UserEntity result = loginService.convertEntity(reqDto);
            assertNotNull(result);
            assertEquals("test@example.com", result.getAccount());
            assertEquals("hashed-password", result.getPassword());
            assertEquals(UserStatusEnum.INACTIVE.getCode(), result.getStatus());
        }
    }

    @Test
    void testConvertToLoginResDto() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setAccount("test@example.com");
        user.setLastLoginTime(LocalDateTime.of(2025, 11, 19, 10, 0));
        ProfileResDto result = loginService.convertToLoginResDto(user);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getAccount());
        assertNotNull(result.getLastLoginTime());
    }
}