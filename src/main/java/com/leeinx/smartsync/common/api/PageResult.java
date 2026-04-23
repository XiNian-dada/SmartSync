package com.leeinx.smartsync.common.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 对分页结果做一次轻量封装，避免直接暴露 MyBatis-Plus 的内部字段。
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {
    /** 总记录数。 */
    private Long total;

    /** 当前页码，从 1 开始。 */
    private Long current;

    /** 每页大小。 */
    private Long size;

    /** 当前页数据。 */
    private List<T> records;

    /** 便捷工厂方法，供 Controller 直接组装分页返回值。 */
    public static <T> PageResult<T> of(long total, long current, long size, List<T> records) {
        PageResult<T> p = new PageResult<>();
        p.setTotal(total);
        p.setCurrent(current);
        p.setSize(size);
        p.setRecords(records);
        return p;
    }
}
