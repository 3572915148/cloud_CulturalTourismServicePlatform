package com.jingdezhen.tourism.merchant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdezhen.tourism.common.dto.MerchantLoginDTO;
import com.jingdezhen.tourism.common.dto.MerchantRegisterDTO;
import com.jingdezhen.tourism.common.dto.MerchantUpdateDTO;
import com.jingdezhen.tourism.common.entity.Merchant;
import com.jingdezhen.tourism.common.vo.MerchantVO;

import java.util.Map;

/**
 * 商户服务接口
 */
public interface MerchantService extends IService<Merchant> {

    /**
     * 商户注册
     */
    MerchantVO register(MerchantRegisterDTO registerDTO);

    /**
     * 商户登录
     */
    Map<String, Object> login(MerchantLoginDTO loginDTO);

    /**
     * 获取当前商户信息
     */
    MerchantVO getMerchantInfo(Long merchantId);

    /**
     * 更新商户信息
     */
    MerchantVO updateMerchantInfo(Long merchantId, MerchantUpdateDTO updateDTO);

    /**
     * 审核通过
     */
    void approve(Long merchantId, String remark);

    /**
     * 审核拒绝
     */
    void reject(Long merchantId, String remark);

    /**
     * 启用/禁用账户
     */
    void changeStatus(Long merchantId, Integer status);
}

