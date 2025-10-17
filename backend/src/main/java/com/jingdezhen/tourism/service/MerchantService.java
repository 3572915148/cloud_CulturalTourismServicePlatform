package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdezhen.tourism.dto.MerchantLoginDTO;
import com.jingdezhen.tourism.dto.MerchantRegisterDTO;
import com.jingdezhen.tourism.dto.MerchantUpdateDTO;
import com.jingdezhen.tourism.entity.Merchant;
import com.jingdezhen.tourism.vo.MerchantVO;

import java.util.Map;

/**
 * 商户服务接口
 */
public interface MerchantService extends IService<Merchant> {

    /**
     * 商户注册
     *
     * @param registerDTO 注册信息
     * @return 注册成功的商户信息
     */
    MerchantVO register(MerchantRegisterDTO registerDTO);

    /**
     * 商户登录
     *
     * @param loginDTO 登录信息
     * @return 包含token和商户信息的Map
     */
    Map<String, Object> login(MerchantLoginDTO loginDTO);

    /**
     * 获取当前商户信息
     *
     * @param merchantId 商户ID
     * @return 商户信息
     */
    MerchantVO getMerchantInfo(Long merchantId);

    /**
     * 更新商户信息
     *
     * @param merchantId 商户ID
     * @param updateDTO  更新信息
     * @return 更新后的商户信息
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

