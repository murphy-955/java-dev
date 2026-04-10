package com.example.mapper;

import com.example.pojo.ClassInfo;
import org.apache.ibatis.annotations.Param;

/**
     * 班级数据访问层接口.
 *
 * @author example
 * @date 2026/04/10
 */
public interface ClassInfoMapper {

    /**
     * 根据ID查询班级信息（包含学生列表）.
     *
     * @param id 班级ID
     * @return 班级对象
     */
    ClassInfo getClassInfoById(@Param("id") Integer id);
}
