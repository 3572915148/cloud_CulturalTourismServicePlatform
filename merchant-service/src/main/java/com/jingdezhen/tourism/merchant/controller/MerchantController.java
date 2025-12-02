package com.jingdezhen.tourism.merchant.controller;

import com.jingdezhen.tourism.common.dto.MerchantLoginDTO;
import com.jingdezhen.tourism.common.dto.MerchantRegisterDTO;
import com.jingdezhen.tourism.common.dto.MerchantUpdateDTO;
import com.jingdezhen.tourism.common.utils.TokenUtil;
import com.jingdezhen.tourism.common.vo.MerchantVO;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.merchant.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 商户控制器
 */
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final TokenUtil tokenUtil;

    /**
     * 商户注册
     */
    @PostMapping("/register")
    public Result<MerchantVO> register(@Validated @RequestBody MerchantRegisterDTO registerDTO) {
        MerchantVO merchantVO = merchantService.register(registerDTO);
        return Result.success("注册成功，请等待管理员审核", merchantVO);
    }

    /**
     * 商户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody MerchantLoginDTO loginDTO) {
        Map<String, Object> result = merchantService.login(loginDTO);
        return Result.success("登录成功", result);
    }

    /**
     * 获取当前商户信息
     */
    @GetMapping("/info")
    public Result<MerchantVO> getMerchantInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Long merchantId = tokenUtil.getUserIdFromAuth(authHeader);
        MerchantVO merchantVO = merchantService.getMerchantInfo(merchantId);
        return Result.success(merchantVO);
    }

    /**
     * 更新商户信息
     */
    @PutMapping("/info")
    public Result<MerchantVO> updateMerchantInfo(HttpServletRequest request,
                                                   @RequestBody MerchantUpdateDTO updateDTO) {
        String authHeader = request.getHeader("Authorization");
        Long merchantId = tokenUtil.getUserIdFromAuth(authHeader);
        MerchantVO merchantVO = merchantService.updateMerchantInfo(merchantId, updateDTO);
        return Result.success("更新成功", merchantVO);
    }
}

