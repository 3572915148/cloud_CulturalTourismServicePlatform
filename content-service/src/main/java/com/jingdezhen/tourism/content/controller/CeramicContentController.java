package com.jingdezhen.tourism.content.controller;

import com.jingdezhen.tourism.common.entity.CeramicContent;
import com.jingdezhen.tourism.common.vo.PageResult;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.content.service.CeramicContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 陶瓷文化内容控制器
 */
@RestController
@RequestMapping("/ceramic")
@RequiredArgsConstructor
public class CeramicContentController {

    private final CeramicContentService ceramicContentService;

    /**
     * 分页查询陶瓷文化内容列表
     */
    @GetMapping("/list")
    public Result<PageResult<CeramicContent>> getContentList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "9") Integer size,
            @RequestParam(required = false) String category) {
        PageResult<CeramicContent> result = ceramicContentService.getContentList(current, size, category);
        return Result.success(result);
    }

    /**
     * 根据ID查询陶瓷文化内容详情
     */
    @GetMapping("/{id}")
    public Result<CeramicContent> getContentById(@PathVariable Long id) {
        CeramicContent content = ceramicContentService.getContentById(id);
        return Result.success(content);
    }

    /**
     * 增加浏览量
     */
    @PostMapping("/{id}/view")
    public Result<Void> incrementViews(@PathVariable Long id) {
        ceramicContentService.incrementViews(id);
        return Result.success();
    }

    /**
     * 获取分类统计信息
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategoryStatistics() {
        List<Map<String, Object>> result = ceramicContentService.getCategoryStatistics();
        return Result.success(result);
    }
}

