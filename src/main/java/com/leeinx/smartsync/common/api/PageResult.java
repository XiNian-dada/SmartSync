package com.leeinx.smartsync.common.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应包装。
 *
 * <h2>为什么不直接返回 MyBatis-Plus 的 {@code IPage}</h2>
 * <ul>
 *   <li>{@code IPage} 是一个带众多内部字段的接口实现，直接序列化给前端会暴露无关字段（orders、searchCount 等）。</li>
 *   <li>我们只关心四件事：<b>总数、当前页、每页条数、数据列表</b>，单独建一个 VO 让 JSON 更干净。</li>
 *   <li>如果将来切换 ORM（比如换成 Spring Data JPA），前端契约不用改。</li>
 * </ul>
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {
    /** 符合查询条件的数据总条数（不是当前页条数） */
    private Long total;

    /** 当前页码，从 1 开始 */
    private Long current;

    /** 每页大小 */
    private Long size;

    /** 当前页的数据列表 */
    private List<T> records;

    /**
     * 便捷工厂：把 MyBatis-Plus 的分页对象字段"平铺"到我们自己的 PageResult。
     * <p>在 Controller 中常这样用：</p>
     * <pre>{@code
     *   IPage<Patient> p = patientService.page(...);
     *   PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords());
     * }</pre>
     *
     * @param total   总条数
     * @param current 当前页码
     * @param size    每页大小
     * @param records 当前页记录
     * @param <T>     元素类型
     * @return 装配好的分页结果
     */
    public static <T> PageResult<T> of(long total, long current, long size, List<T> records) {
        PageResult<T> p = new PageResult<>();
        p.setTotal(total);
        p.setCurrent(current);
        p.setSize(size);
        p.setRecords(records);
        return p;
    }
}
