package com.example.service;

import com.example.pojo.ClassInfo;

/**
 * 班级服务层接口.
 *
 * @author example
 * @date 2026/04/10
 */
public interface ClassInfoService {

    /**
     * 根据ID获取班级信息.
     *
     * @param id 班级ID
     * @return 班级对象
     */
    ClassInfo getClassInfoById(Integer id);
}
