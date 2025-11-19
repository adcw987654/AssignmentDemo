package com.ampgo.demo.mapper;

import com.ampgo.demo.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    Optional<UserEntity> findByAccount(@Param("account") String account);

    Optional<UserEntity> findById(@Param("id") Long id);

    Integer countByAccountOrEmail(@Param("account") String account);

    void insert(UserEntity user);

    void updateLastLoginTime(Long id);

    void updateVerifyStatus(String account, int status);
}
