package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingdezhen.tourism.dto.MerchantLoginDTO;
import com.jingdezhen.tourism.dto.MerchantRegisterDTO;
import com.jingdezhen.tourism.dto.MerchantUpdateDTO;
import com.jingdezhen.tourism.entity.Merchant;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.MerchantMapper;
import com.jingdezhen.tourism.service.MerchantService;
import com.jingdezhen.tourism.utils.JwtUtil;
import com.jingdezhen.tourism.utils.PasswordUtil;
import com.jingdezhen.tourism.vo.MerchantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 商户服务实现类
 */
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant>
        implements MerchantService {

    private final JwtUtil jwtUtil;

    @Override
    public MerchantVO register(MerchantRegisterDTO registerDTO) {
        // 检查商户账号是否已存在
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUsername, registerDTO.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("该商户账号已存在");
        }

        // 创建商户实体
        Merchant merchant = new Merchant();
        merchant.setUsername(registerDTO.getUsername());
        merchant.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        merchant.setShopName(registerDTO.getShopName());
        merchant.setContactPerson(registerDTO.getContactPerson());
        merchant.setContactPhone(registerDTO.getContactPhone());
        merchant.setContactEmail(registerDTO.getContactEmail());
        merchant.setAddress(registerDTO.getAddress());
        merchant.setShopIntroduction(registerDTO.getShopIntroduction());
        merchant.setAuditStatus(0); // 待审核
        merchant.setStatus(1); // 正常状态

        // 保存到数据库
        boolean saved = this.save(merchant);
        if (!saved) {
            throw new BusinessException("注册失败");
        }

        // 转换为VO返回
        return convertToVO(merchant);
    }

    @Override
    public Map<String, Object> login(MerchantLoginDTO loginDTO) {
        // 查询商户
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUsername, loginDTO.getUsername());
        Merchant merchant = this.getOne(wrapper);

        // 验证账号和密码
        if (merchant == null) {
            throw new BusinessException("商户账号不存在");
        }
        
        if (!PasswordUtil.matches(loginDTO.getPassword(), merchant.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查账户状态
        if (merchant.getStatus() == 0) {
            throw new BusinessException("该商户账号已被禁用");
        }

        // 检查审核状态（可选：如果需要审核通过才能登录）
        // if (merchant.getAuditStatus() == 0) {
        //     throw new BusinessException("商户正在审核中，请耐心等待");
        // }
        // if (merchant.getAuditStatus() == 2) {
        //     throw new BusinessException("商户审核未通过：" + merchant.getAuditRemark());
        // }

        // 生成Token（使用"merchant"作为用户类型标识）
        String token = jwtUtil.generateToken(merchant.getId(), merchant.getUsername(), "merchant");

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("merchantInfo", convertToVO(merchant));

        return result;
    }

    @Override
    public MerchantVO getMerchantInfo(Long merchantId) {
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        return convertToVO(merchant);
    }

    @Override
    public MerchantVO updateMerchantInfo(Long merchantId, MerchantUpdateDTO updateDTO) {
        // 查询商户
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }

        // 更新信息
        if (StringUtils.hasText(updateDTO.getShopName())) {
            merchant.setShopName(updateDTO.getShopName());
        }
        if (updateDTO.getShopLogo() != null) {
            merchant.setShopLogo(updateDTO.getShopLogo());
        }
        if (updateDTO.getShopIntroduction() != null) {
            merchant.setShopIntroduction(updateDTO.getShopIntroduction());
        }
        if (StringUtils.hasText(updateDTO.getContactPerson())) {
            merchant.setContactPerson(updateDTO.getContactPerson());
        }
        if (StringUtils.hasText(updateDTO.getContactPhone())) {
            merchant.setContactPhone(updateDTO.getContactPhone());
        }
        if (updateDTO.getContactEmail() != null) {
            merchant.setContactEmail(updateDTO.getContactEmail());
        }
        if (updateDTO.getAddress() != null) {
            merchant.setAddress(updateDTO.getAddress());
        }

        // 保存更新
        boolean updated = this.updateById(merchant);
        if (!updated) {
            throw new BusinessException("更新失败");
        }

        return convertToVO(merchant);
    }

    /**
     * 将Merchant实体转换为MerchantVO
     */
    private MerchantVO convertToVO(Merchant merchant) {
        MerchantVO vo = new MerchantVO();
        BeanUtils.copyProperties(merchant, vo);
        vo.setAuditStatusText();
        return vo;
    }
}

