package com.jingdezhen.tourism.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单DTO
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    /**
     * 使用日期（可选）
     */
    private String useDate;

    /**
     * 使用人数（可选）
     */
    private Integer useCount;

    /**
     * 备注（可选）
     */
    private String remark;
}

