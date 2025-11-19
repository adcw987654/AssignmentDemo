package com.ampgo.demo.service;

import com.ampgo.demo.bo.UserAccountBo;
import org.springframework.stereotype.Service;

@Service
public interface UserValidService {

    boolean isUserExist(UserAccountBo userBo);
}
