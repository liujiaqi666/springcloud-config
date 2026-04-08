package com.exam.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_exam_records")
public class StudentExamRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long arrangementId;
    
    private Long studentId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime submitTime;
    
    private BigDecimal totalScore;
    
    private String status; // NOT_STARTED, IN_PROGRESS, SUBMITTED, GRADED
    
    private String ipAddress;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
