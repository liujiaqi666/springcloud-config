package com.exam.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_papers")
public class ExamPaper implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String paperName;
    
    private Long courseId;
    
    private BigDecimal totalScore;
    
    private BigDecimal passScore;
    
    private Integer durationMinutes;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String status; // DRAFT, PUBLISHED, CLOSED
    
    private String config; // JSON
    
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
