package com.jingdezhen.tourism.common.dto;

import lombok.Data;

/**
 * 商户信息更新DTO
 */
@Data
public class MerchantUpdateDTO {
    private String shopName;
    private String shopLogo;
    private String shopIntroduction;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String address;
}

