package com.jingdezhen.tourism.controller;

import com.jingdezhen.tourism.entity.CeramicContent;
import com.jingdezhen.tourism.service.CeramicContentService;
import com.jingdezhen.tourism.vo.PageResult;
import com.jingdezhen.tourism.vo.Result;
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
     *
     * @param current  当前页，默认1
     * @param size     每页大小，默认9
     * @param category 分类（可选）：all-全部、history-陶瓷历史、craft-制作工艺、master-陶瓷名家、culture-文化传承
     * @return 分页结果
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
     *
     * @param id 内容ID
     * @return 内容详情
     */
    @GetMapping("/{id}")
    public Result<CeramicContent> getContentById(@PathVariable Long id) {
        CeramicContent content = ceramicContentService.getContentById(id);
        return Result.success(content);
    }

    /**
     * 增加浏览量
     *
     * @param id 内容ID
     * @return 操作结果
     */
    @PostMapping("/{id}/view")
    public Result<Void> incrementViews(@PathVariable Long id) {
        ceramicContentService.incrementViews(id);
        return Result.success();
    }

    /**
     * 获取分类统计信息
     *
     * @return 分类列表及每个分类的内容数量
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategoryStatistics() {
        List<Map<String, Object>> result = ceramicContentService.getCategoryStatistics();
        return Result.success(result);
    }
}

