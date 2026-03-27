package com.example.mapper;

import com.example.pojo.ClassInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 班级数据访问层接口
 *
 * @author 李泽聿
 */
public interface ClassInfoMapper {
    
    /**
     * 根据ID查询班级信息
     * @param id 班级ID
     * @return 班级信息
     */
    ClassInfo findById(@Param("id") Integer id);
    
    /**
     * 查询所有班级信息
     * @return 班级列表
     */
    List<ClassInfo> findAll();
    
    /**
     * 插入班级信息
     * @param classInfo 班级信息
     * @return 影响行数
     */
    int insert(ClassInfo classInfo);
}
