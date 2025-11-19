package com.ampgo.demo.service.impl;

import com.ampgo.demo.bo.UserAccountBo;
import com.ampgo.demo.mapper.UserMapper;
import com.ampgo.demo.service.UserValidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserValidServiceImpl implements UserValidService {

    private final UserMapper userMapper;

    public boolean isUserExist(UserAccountBo userBo) {
        Integer count = userMapper.countByAccountOrEmail(userBo.getAccount());
        return count > 0;
    }
}
