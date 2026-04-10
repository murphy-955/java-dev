package com.example.service.impl;

import com.example.mapper.ClassInfoMapper;
import com.example.pojo.ClassInfo;
import com.example.service.ClassInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 班级服务层实现类.
 *
 * @author example
 * @date 2026/04/10
 */
@Service
public class ClassInfoServiceImpl implements ClassInfoService {

    private final ClassInfoMapper classInfoMapper;

    @Autowired
    public ClassInfoServiceImpl(ClassInfoMapper classInfoMapper) {
        this.classInfoMapper = classInfoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ClassInfo getClassInfoById(Integer id) {
        return classInfoMapper.getClassInfoById(id);
    }
}
