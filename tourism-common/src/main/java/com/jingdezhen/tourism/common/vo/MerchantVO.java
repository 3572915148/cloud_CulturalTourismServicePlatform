package com.jingdezhen.tourism.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商户信息VO
 */
@Data
public class MerchantVO {
    private Long id;
    private String username;
    private String shopName;
    private String shopLogo;
    private String shopIntroduction;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private Integer auditStatus;
    private String auditStatusText;
    private String auditRemark;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public void setAuditStatusText() {
        if (this.auditStatus == null) {
            this.auditStatusText = "未知";
            return;
        }
        switch (this.auditStatus) {
            case 0: this.auditStatusText = "待审核"; break;
            case 1: this.auditStatusText = "审核通过"; break;
            case 2: this.auditStatusText = "审核拒绝"; break;
            default: this.auditStatusText = "未知";
        }
    }
}

