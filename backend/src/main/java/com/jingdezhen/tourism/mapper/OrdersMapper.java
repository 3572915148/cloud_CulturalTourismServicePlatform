package com.jingdezhen.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}

