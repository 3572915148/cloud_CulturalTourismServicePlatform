package com.jingdezhen.tourism.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingdezhen.tourism.common.entity.CeramicContent;
import com.jingdezhen.tourism.common.exception.BusinessException;
import com.jingdezhen.tourism.common.vo.PageResult;
import com.jingdezhen.tourism.content.mapper.CeramicContentMapper;
import com.jingdezhen.tourism.content.service.CeramicContentService;
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
        LambdaQueryWrapper<CeramicContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CeramicContent::getStatus, 1);
        
        if (StringUtils.hasText(category) && !"all".equals(category)) {
            wrapper.eq(CeramicContent::getCategory, category);
        }
        
        wrapper.orderByAsc(CeramicContent::getSortOrder)
               .orderByDesc(CeramicContent::getCreateTime);

        Page<CeramicContent> page = new Page<>(current, size);
        Page<CeramicContent> result = this.page(page, wrapper);

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
        LambdaQueryWrapper<CeramicContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CeramicContent::getId, id)
               .eq(CeramicContent::getStatus, 1);

        CeramicContent content = this.getOne(wrapper);
        if (content == null) {
            throw new BusinessException("内容不存在或已下架");
        }

        return content;
    }

    @Override
    public void incrementViews(Long id) {
        CeramicContent content = this.getById(id);
        if (content == null) {
            throw new BusinessException("内容不存在");
        }
        baseMapper.incrementViews(id);
    }

    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        LambdaQueryWrapper<CeramicContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CeramicContent::getStatus, 1)
               .select(CeramicContent::getCategory);

        List<CeramicContent> allContent = this.list(wrapper);

        Map<String, Integer> categoryCountMap = new HashMap<>();
        for (CeramicContent content : allContent) {
            String category = content.getCategory();
            categoryCountMap.put(category, categoryCountMap.getOrDefault(category, 0) + 1);
        }

        Map<String, String> categoryNameMap = new HashMap<>();
        categoryNameMap.put("history", "陶瓷历史");
        categoryNameMap.put("craft", "制作工艺");
        categoryNameMap.put("master", "陶瓷名家");
        categoryNameMap.put("culture", "文化传承");

        List<Map<String, Object>> result = new ArrayList<>();
        
        Map<String, Object> allCategory = new HashMap<>();
        allCategory.put("key", "all");
        allCategory.put("label", "全部");
        allCategory.put("count", allContent.size());
        result.add(allCategory);

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

