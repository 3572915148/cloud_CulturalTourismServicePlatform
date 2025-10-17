package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.entity.Merchant;
import com.jingdezhen.tourism.service.MerchantService;
import com.jingdezhen.tourism.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-商户审核与管理（简单版，未做权限认证网关）
 */
@RestController
@RequestMapping("/admin/merchant")
@RequiredArgsConstructor
public class AdminMerchantAuditController {

    private final MerchantService merchantService;

    /**
     * 待审核/全部商户列表
     */
    @GetMapping("/list")
    public Result<Page<Merchant>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer auditStatus
    ) {
        Page<Merchant> page = new Page<>(current, size);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) wrapper.eq(Merchant::getAuditStatus, auditStatus);
        Page<Merchant> result = merchantService.page(page, wrapper);
        return Result.success(result);
    }

    /** 审核通过 */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestParam(required = false) String remark) {
        merchantService.approve(id, remark);
        return Result.success("审核通过");
    }

    /** 审核拒绝 */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String remark) {
        merchantService.reject(id, remark);
        return Result.success("已拒绝");
    }

    /** 启用/禁用 */
    @PostMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        merchantService.changeStatus(id, status);
        return Result.success("状态已更新");
    }
}


