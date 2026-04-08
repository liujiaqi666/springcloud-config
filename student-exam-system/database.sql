-- 学生考试系统数据库脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE exam_system;

-- 用户表 (学生/教师/管理员)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL DEFAULT 'STUDENT' COMMENT '角色',
    email VARCHAR(100),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1 COMMENT '状态:1正常,0禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB COMMENT='用户表';

-- 班级表
CREATE TABLE classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    grade VARCHAR(20) COMMENT '年级',
    teacher_id BIGINT COMMENT '班主任ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_class_name (class_name)
) ENGINE=InnoDB COMMENT='班级表';

-- 学生班级关联表
CREATE TABLE student_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    enroll_date DATE COMMENT '入学日期',
    UNIQUE KEY uk_student_class (student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    INDEX idx_class_id (class_id)
) ENGINE=InnoDB COMMENT='学生班级关联表';

-- 课程表
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_code VARCHAR(20) UNIQUE NOT NULL COMMENT '课程代码',
    description TEXT COMMENT '课程描述',
    teacher_id BIGINT COMMENT '任课教师ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_course_code (course_code)
) ENGINE=InnoDB COMMENT='课程表';

-- 题库表
CREATE TABLE question_bank (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    question_type ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'JUDGMENT', 'SHORT_ANSWER') NOT NULL COMMENT '题型',
    content TEXT NOT NULL COMMENT '题目内容',
    options JSON COMMENT '选项(选择题用)',
    answer TEXT NOT NULL COMMENT '正确答案',
    analysis TEXT COMMENT '答案解析',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' COMMENT '难度',
    score DECIMAL(5,2) DEFAULT 0 COMMENT '默认分值',
    tags VARCHAR(255) COMMENT '标签,逗号分隔',
    status TINYINT DEFAULT 1 COMMENT '状态:1启用,0禁用',
    created_by BIGINT COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_course_id (course_id),
    INDEX idx_question_type (question_type),
    INDEX idx_difficulty (difficulty)
) ENGINE=InnoDB COMMENT='题库表';

-- 试卷表
CREATE TABLE exam_papers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    paper_name VARCHAR(100) NOT NULL COMMENT '试卷名称',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    total_score DECIMAL(5,2) NOT NULL COMMENT '总分',
    pass_score DECIMAL(5,2) COMMENT '及格分',
    duration_minutes INT NOT NULL COMMENT '考试时长(分钟)',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED') DEFAULT 'DRAFT' COMMENT '状态',
    config JSON COMMENT '配置(随机策略等)',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_course_id (course_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB COMMENT='试卷表';

-- 试卷题目关联表
CREATE TABLE exam_paper_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    paper_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL COMMENT '题目顺序',
    score DECIMAL(5,2) NOT NULL COMMENT '本题分值',
    UNIQUE KEY uk_paper_question (paper_id, question_id),
    FOREIGN KEY (paper_id) REFERENCES exam_papers(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question_bank(id) ON DELETE CASCADE,
    INDEX idx_paper_id (paper_id)
) ENGINE=InnoDB COMMENT='试卷题目关联表';

-- 考试安排表
CREATE TABLE exam_arrangements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    paper_id BIGINT NOT NULL COMMENT '试卷ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    start_time DATETIME NOT NULL COMMENT '考试开始时间',
    end_time DATETIME NOT NULL COMMENT '考试结束时间',
    duration_minutes INT NOT NULL COMMENT '允许答题时长',
    status ENUM('SCHEDULED', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    remark VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paper_id) REFERENCES exam_papers(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    INDEX idx_class_id (class_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='考试安排表';

-- 学生考试记录表
CREATE TABLE student_exam_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    arrangement_id BIGINT NOT NULL COMMENT '考试安排ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    start_time DATETIME COMMENT '实际开始时间',
    submit_time DATETIME COMMENT '提交时间',
    total_score DECIMAL(5,2) COMMENT '总分',
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'SUBMITTED', 'GRADED') DEFAULT 'NOT_STARTED',
    ip_address VARCHAR(50) COMMENT '考试IP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_arrangement_student (arrangement_id, student_id),
    FOREIGN KEY (arrangement_id) REFERENCES exam_arrangements(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='学生考试记录表';

-- 学生答案表
CREATE TABLE student_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL COMMENT '考试记录ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer TEXT COMMENT '学生答案',
    is_correct TINYINT COMMENT '是否正确(1是,0否,NULL未批改)',
    score DECIMAL(5,2) DEFAULT 0 COMMENT '得分',
    teacher_comment TEXT COMMENT '教师评语',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_record_question (record_id, question_id),
    FOREIGN KEY (record_id) REFERENCES student_exam_records(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question_bank(id) ON DELETE CASCADE,
    INDEX idx_record_id (record_id)
) ENGINE=InnoDB COMMENT='学生答案表';

-- 考试成绩统计表
CREATE TABLE exam_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    arrangement_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    total_students INT DEFAULT 0 COMMENT '总人数',
    participated INT DEFAULT 0 COMMENT '参加人数',
    avg_score DECIMAL(5,2) COMMENT '平均分',
    max_score DECIMAL(5,2) COMMENT '最高分',
    min_score DECIMAL(5,2) COMMENT '最低分',
    pass_rate DECIMAL(5,2) COMMENT '及格率',
    score_distribution JSON COMMENT '分数段分布',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_arrangement_class (arrangement_id, class_id),
    FOREIGN KEY (arrangement_id) REFERENCES exam_arrangements(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='考试成绩统计表';

-- 初始数据
INSERT INTO users (username, password, real_name, role, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN', 'admin@exam.com', 1),
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张老师', 'TEACHER', 'teacher1@exam.com', 1),
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李明', 'STUDENT', 'student1@exam.com', 1),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王芳', 'STUDENT', 'student2@exam.com', 1);

INSERT INTO classes (class_name, grade, teacher_id) VALUES
('高一(1)班', '高一', 2),
('高一(2)班', '高一', 2);

INSERT INTO courses (course_name, course_code, description, teacher_id) VALUES
('数学', 'MATH001', '高中数学课程', 2),
('英语', 'ENG001', '高中英语课程', 2);

-- 关联学生到班级
INSERT INTO student_class (student_id, class_id, enroll_date) VALUES
(3, 1, CURDATE()),
(4, 1, CURDATE());

-- 示例题目
INSERT INTO question_bank (course_id, question_type, content, options, answer, analysis, difficulty, score, created_by) VALUES
(1, 'SINGLE_CHOICE', '已知集合A={1,2,3}, B={2,3,4}, 则A∩B等于？', 
 '["A. {1,2}", "B. {2,3}", "C. {3,4}", "D. {1,4}"]', 
 'B', '交集是同时属于两个集合的元素', 'EASY', 5.0, 2),
(1, 'JUDGMENT', '函数的定义域是指自变量的取值范围。', 
 NULL, 'TRUE', '这是函数定义域的基本定义', 'EASY', 5.0, 2);
