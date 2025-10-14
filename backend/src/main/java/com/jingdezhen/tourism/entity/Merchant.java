package com.jingdezhen.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户实体类
 */
@Data
@TableName("merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String shopName;

    private String shopLogo;

    private String shopIntroduction;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String businessLicense;

    private String idCardFront;

    private String idCardBack;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核拒绝
     */
    private Integer auditStatus;

    private String auditRemark;

    /**
     * 账户状态：0-禁用，1-正常
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

