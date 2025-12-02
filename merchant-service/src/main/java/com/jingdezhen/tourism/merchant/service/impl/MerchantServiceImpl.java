package com.jingdezhen.tourism.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingdezhen.tourism.common.dto.MerchantLoginDTO;
import com.jingdezhen.tourism.common.dto.MerchantRegisterDTO;
import com.jingdezhen.tourism.common.dto.MerchantUpdateDTO;
import com.jingdezhen.tourism.common.entity.Merchant;
import com.jingdezhen.tourism.common.exception.BusinessException;
import com.jingdezhen.tourism.common.utils.JwtUtil;
import com.jingdezhen.tourism.common.utils.PasswordUtil;
import com.jingdezhen.tourism.common.vo.MerchantVO;
import com.jingdezhen.tourism.merchant.mapper.MerchantMapper;
import com.jingdezhen.tourism.merchant.service.MerchantService;
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

    @Override
    public void approve(Long merchantId, String remark) {
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) throw new BusinessException("商户不存在");
        merchant.setAuditStatus(1);
        merchant.setAuditRemark(remark);
        this.updateById(merchant);
    }

    @Override
    public void reject(Long merchantId, String remark) {
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) throw new BusinessException("商户不存在");
        merchant.setAuditStatus(2);
        merchant.setAuditRemark(remark);
        this.updateById(merchant);
    }

    @Override
    public void changeStatus(Long merchantId, Integer status) {
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) throw new BusinessException("商户不存在");
        merchant.setStatus(status);
        this.updateById(merchant);
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

