package com.ampgo.demo.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserAccountBo {

    private String account;
}
