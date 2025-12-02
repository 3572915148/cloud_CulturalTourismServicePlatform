package com.jingdezhen.tourism.common.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建评价DTO
 */
@Data
public class ReviewCreateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 500, message = "评价内容不能超过500字")
    private String content;

    private String images;
    private Boolean anonymous = false;
}

