package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingdezhen.tourism.entity.CeramicContent;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.CeramicContentMapper;
import com.jingdezhen.tourism.service.CeramicContentService;
import com.jingdezhen.tourism.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 陶瓷文化内容服务实现类
 */
@Service
public class CeramicContentServiceImpl extends ServiceImpl<CeramicContentMapper, CeramicContent>
        implements CeramicContentService {

    @Override
    public PageResult<CeramicContent> getContentList(Integer current, Integer size, String category) {
        // 构建查询条件
        LambdaQueryWrapper<CeramicContent> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询已上架的内容
        wrapper.eq(CeramicContent::getStatus, 1);
        
        // 如果指定了分类，添加分类查询条件
        if (StringUtils.hasText(category) && !"all".equals(category)) {
            wrapper.eq(CeramicContent::getCategory, category);
        }
        
        // 按排序字段排序，排序字段相同时按创建时间倒序
        wrapper.orderByAsc(CeramicContent::getSortOrder)
               .orderByDesc(CeramicContent::getCreateTime);

        // 分页查询
        Page<CeramicContent> page = new Page<>(current, size);
        Page<CeramicContent> result = this.page(page, wrapper);

        // 封装返回结果
        return new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                result.getCurrent(),
                result.getSize(),
                result.getPages()
        );
    }

    @Override
    public CeramicContent getContentById(Long id) {
        // 查询内容详情
        LambdaQueryWrapper<CeramicContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CeramicContent::getId, id)
               .eq(CeramicContent::getStatus, 1); // 只查询已上架的内容

        CeramicContent content = this.getOne(wrapper);
        if (content == null) {
            throw new BusinessException("内容不存在或已下架");
        }

        return content;
    }

    @Override
    public void incrementViews(Long id) {
        // 先检查内容是否存在
        CeramicContent content = this.getById(id);
        if (content == null) {
            throw new BusinessException("内容不存在");
        }

        // 增加浏览量
        baseMapper.incrementViews(id);
    }

    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        // 查询所有已上架的内容
        LambdaQueryWrapper<CeramicContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CeramicContent::getStatus, 1)
               .select(CeramicContent::getCategory);

        List<CeramicContent> allContent = this.list(wrapper);

        // 统计各分类的数量
        Map<String, Integer> categoryCountMap = new HashMap<>();
        for (CeramicContent content : allContent) {
            String category = content.getCategory();
            categoryCountMap.put(category, categoryCountMap.getOrDefault(category, 0) + 1);
        }

        // 定义分类信息
        Map<String, String> categoryNameMap = new HashMap<>();
        categoryNameMap.put("history", "陶瓷历史");
        categoryNameMap.put("craft", "制作工艺");
        categoryNameMap.put("master", "陶瓷名家");
        categoryNameMap.put("culture", "文化传承");

        // 构建返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 添加"全部"分类
        Map<String, Object> allCategory = new HashMap<>();
        allCategory.put("key", "all");
        allCategory.put("label", "全部");
        allCategory.put("count", allContent.size());
        result.add(allCategory);

        // 添加各个具体分类
        for (Map.Entry<String, String> entry : categoryNameMap.entrySet()) {
            String key = entry.getKey();
            String label = entry.getValue();
            int count = categoryCountMap.getOrDefault(key, 0);

            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("key", key);
            categoryInfo.put("label", label);
            categoryInfo.put("count", count);
            result.add(categoryInfo);
        }

        return result;
    }
}

