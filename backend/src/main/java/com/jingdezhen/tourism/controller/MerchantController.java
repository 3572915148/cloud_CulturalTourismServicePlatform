package com.jingdezhen.tourism.controller;

import com.jingdezhen.tourism.dto.MerchantLoginDTO;
import com.jingdezhen.tourism.dto.MerchantRegisterDTO;
import com.jingdezhen.tourism.dto.MerchantUpdateDTO;
import com.jingdezhen.tourism.service.MerchantService;
import com.jingdezhen.tourism.utils.TokenUtil;
import com.jingdezhen.tourism.vo.MerchantVO;
import com.jingdezhen.tourism.vo.Result;
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
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<MerchantVO> register(@Validated @RequestBody MerchantRegisterDTO registerDTO) {
        MerchantVO merchantVO = merchantService.register(registerDTO);
        return Result.success("注册成功，请等待管理员审核", merchantVO);
    }

    /**
     * 商户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（包含token和商户信息）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody MerchantLoginDTO loginDTO) {
        Map<String, Object> result = merchantService.login(loginDTO);
        return Result.success("登录成功", result);
    }

    /**
     * 获取当前商户信息
     *
     * @param request HTTP请求
     * @return 商户信息
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
     *
     * @param request   HTTP请求
     * @param updateDTO 更新信息
     * @return 更新后的商户信息
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

