package com.ampgo.demo.mapper;

import com.ampgo.demo.entity.VerificationCodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VerificationCodeMapper {

    void insert(VerificationCodeEntity entity);

    VerificationCodeEntity findByEmailAndCode(@Param("email") String email, @Param("code") String code);

    void updateVerified(@Param("id") Long id);
}
