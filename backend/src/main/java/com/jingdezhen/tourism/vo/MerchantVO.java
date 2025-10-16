package com.jingdezhen.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户信息VO
 */
@Data
public class MerchantVO {

    /**
     * 商户ID
     */
    private Long id;

    /**
     * 商户账号
     */
    private String username;

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

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核拒绝
     */
    private Integer auditStatus;

    /**
     * 审核状态文本
     */
    private String auditStatusText;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 账户状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 设置审核状态文本
     */
    public void setAuditStatusText() {
        if (this.auditStatus == null) {
            this.auditStatusText = "未知";
            return;
        }
        switch (this.auditStatus) {
            case 0:
                this.auditStatusText = "待审核";
                break;
            case 1:
                this.auditStatusText = "审核通过";
                break;
            case 2:
                this.auditStatusText = "审核拒绝";
                break;
            default:
                this.auditStatusText = "未知";
        }
    }
}

