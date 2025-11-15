package com.jingdezhen.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 产品分类Mapper
 * SQL语句定义在 ProductCategoryMapper.xml 中
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
    
    /**
     * 获取分类统计信息（包含每个分类的产品数量）
     * 
     * @return 分类统计信息列表
     */
    List<Map<String, Object>> getCategoryStatistics();
}


