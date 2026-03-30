package com.example.service.impl;

import com.example.annotation.OperationLog;
import com.example.common.Result;
import com.example.common.XssUtils;
import com.example.enums.OperationEnum;
import com.example.mapper.StudentMapper;
import com.example.pojo.Student;
import com.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 学生服务实现类
 *
 * @author 李泽聿
 */
@Service("studentService")
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    @Autowired
    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    /**
     * 根据ID查询学生信息
     */
    @Override
    @OperationLog(OperationEnum.QUERY)
    @Transactional(readOnly = true)
    public Result<Student> findStudentById(Integer id) {
        if (id == null || id <= 0) {
            return Result.badRequest("学生ID不能为空或必须大于0");
        }
        Student student = studentMapper.findById(id);
        if (student == null) {
            return Result.notFound("学生不存在");
        }
        // 对返回的数据进行XSS编码，防止反射型XSS
        sanitizeStudentOutput(student);
        return Result.success(student);
    }

    /**
     * 查询所有学生信息
     */
    @Override
    @OperationLog(value = OperationEnum.QUERY, desc = "查询所有学生列表")
    @Transactional(readOnly = true)
    public Result<List<Student>> findAllStudents() {
        List<Student> students = studentMapper.findAll();
        // 对所有返回的数据进行XSS编码
        students.forEach(this::sanitizeStudentOutput);
        return Result.success(students);
    }

    /**
     * 更新学生信息（声明式事务）
     */
    @Override
    @OperationLog(value = OperationEnum.UPDATE, desc = "更新学生信息", saveParams = true)
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateStudent(Integer id, String name, Integer age) {
        // 参数校验
        if (id == null || id <= 0) {
            return Result.badRequest("学生ID不能为空或必须大于0");
        }
        if (!StringUtils.hasText(name)) {
            return Result.badRequest("学生姓名不能为空");
        }
        if (age == null || age < 0 || age > 150) {
            return Result.badRequest("学生年龄不合法");
        }

        // 检查学生是否存在
        Student existingStudent = studentMapper.findById(id);
        if (existingStudent == null) {
            return Result.notFound("学生不存在");
        }

        // 更新学生信息（对用户输入进行XSS过滤）
        Student student = new Student();
        student.setId(id);
        student.setName(XssUtils.sanitize(name));
        student.setAge(age);

        int rows = studentMapper.update(student);
        if (rows > 0) {
            return Result.success("更新成功", null);
        } else {
            return Result.serverError("更新失败");
        }
    }

    /**
     * 添加学生（声明式事务）
     */
    @Override
    @OperationLog(value = OperationEnum.CREATE, desc = "添加新学生", saveParams = true)
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> addStudent(Student student) {
        // 参数校验
        if (student == null) {
            return Result.badRequest("学生信息不能为空");
        }
        if (!StringUtils.hasText(student.getName())) {
            return Result.badRequest("学生姓名不能为空");
        }
        if (student.getAge() == null || student.getAge() < 0 || student.getAge() > 150) {
            return Result.badRequest("学生年龄不合法");
        }
        if (student.getClassId() == null || student.getClassId() <= 0) {
            return Result.badRequest("班级ID不能为空或必须大于0");
        }

        // 对用户输入进行XSS过滤
        student.setName(XssUtils.sanitize(student.getName()));

        int rows = studentMapper.insert(student);
        if (rows > 0) {
            return Result.created(student.getId());
        } else {
            return Result.serverError("添加失败");
        }
    }

    /**
     * 删除学生（声明式事务）
     */
    @Override
    @OperationLog(value = OperationEnum.DELETE, desc = "删除学生信息")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteStudent(Integer id) {
        // 参数校验
        if (id == null || id <= 0) {
            return Result.badRequest("学生ID不能为空或必须大于0");
        }

        // 检查学生是否存在
        Student existingStudent = studentMapper.findById(id);
        if (existingStudent == null) {
            return Result.notFound("学生不存在");
        }

        int rows = studentMapper.deleteById(id);
        if (rows > 0) {
            return Result.success("删除成功", null);
        } else {
            return Result.serverError("删除失败");
        }
    }

    /**
     * 对学生数据进行输出清理，防止XSS
     * 对可能包含用户输入的字符串字段进行HTML实体编码
     */
    private void sanitizeStudentOutput(Student student) {
        if (student == null) {
            return;
        }
        if (StringUtils.hasText(student.getName())) {
            student.setName(XssUtils.encode(student.getName()));
        }
        // 清理关联的班级信息
        if (student.getClassInfo() != null && StringUtils.hasText(student.getClassInfo().getClassName())) {
            student.getClassInfo().setClassName(XssUtils.encode(student.getClassInfo().getClassName()));
        }
    }
}
