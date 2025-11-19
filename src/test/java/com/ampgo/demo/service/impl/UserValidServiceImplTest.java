package com.ampgo.demo.service.impl;

import com.ampgo.demo.bo.UserAccountBo;
import com.ampgo.demo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserValidServiceImpl userValidService;

    @Test
    void testIsUserExist_UserExists() {
        UserAccountBo userBo = UserAccountBo.builder()
                .account("test@example.com")
                .build();
        when(userMapper.countByAccountOrEmail("test@example.com")).thenReturn(1);
        boolean result = userValidService.isUserExist(userBo);
        assertTrue(result);
        verify(userMapper, times(1)).countByAccountOrEmail("test@example.com");
    }

    @Test
    void testIsUserExist_UserDoesNotExist() {
        UserAccountBo userBo = UserAccountBo.builder()
                .account("nonexistent@example.com")
                .build();
        when(userMapper.countByAccountOrEmail("nonexistent@example.com")).thenReturn(0);
        boolean result = userValidService.isUserExist(userBo);
        assertFalse(result);
        verify(userMapper, times(1)).countByAccountOrEmail("nonexistent@example.com");
    }

    @Test
    void testIsUserExist_MultipleUsersExist() {
        UserAccountBo userBo = UserAccountBo.builder()
                .account("test@example.com")
                .build();
        when(userMapper.countByAccountOrEmail("test@example.com")).thenReturn(5);
        boolean result = userValidService.isUserExist(userBo);
        assertTrue(result);
        verify(userMapper, times(1)).countByAccountOrEmail("test@example.com");
    }

}
