package com.jingdezhen.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}

