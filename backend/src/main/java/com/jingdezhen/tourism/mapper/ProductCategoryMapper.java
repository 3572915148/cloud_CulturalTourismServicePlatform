package com.jingdezhen.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
    
    /**
     * 获取分类统计信息（包含每个分类的产品数量）
     */
    @Select("SELECT " +
            "pc.id, " +
            "pc.name, " +
            "pc.icon, " +
            "pc.sort_order, " +
            "COALESCE(COUNT(p.id), 0) as product_count " +
            "FROM product_category pc " +
            "LEFT JOIN product p ON pc.id = p.category_id AND p.status = 1 AND p.deleted = 0 " +
            "WHERE pc.deleted = 0 " +
            "GROUP BY pc.id, pc.name, pc.icon, pc.sort_order " +
            "ORDER BY pc.sort_order ASC, pc.id ASC")
    List<Map<String, Object>> getCategoryStatistics();
}


