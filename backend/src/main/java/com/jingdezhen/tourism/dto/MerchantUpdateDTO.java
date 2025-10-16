package com.jingdezhen.tourism.dto;

import lombok.Data;

/**
 * 商户信息更新DTO
 */
@Data
public class MerchantUpdateDTO {

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺Logo
     */
    private String shopLogo;

    /**
     * 店铺介绍
     */
    private String shopIntroduction;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 店铺地址
     */
    private String address;
}

