package com.jingdezhen.tourism.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果类
 * @author shirenan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;
}

