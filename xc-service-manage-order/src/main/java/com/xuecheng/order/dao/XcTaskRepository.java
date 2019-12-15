package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;

public interface XcTaskRepository extends JpaRepository<XcTask, String> {

    // 查询某个时间之前的前N条任务
    // Before是一个关键字,表示查询在此之前的数据
    // 分页查询的参数和查询的时间(因为要查询在此之前的时间)
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);

    // 更新updateTime
    @Modifying  // 表示要修改数据  springDataJpa的Sql语句,因为是面向对象的所以用的是对象 XcTask
    @Query("update XcTask t set t.updateTime = :updateTime where t.id = :id ")
    public int updateTaskTime(@Param(value = "id") String id, @Param(value = "updateTime")Date updateTime);

    @Modifying  // 通过id和版本号相结合的方式寻找到对象
    @Query("update XcTask t set t.version = :version+1 where t.id = :id and t.version = :version")
    public int updateTaskVersion(@Param(value = "id") String id,@Param(value = "version") int version);
}
