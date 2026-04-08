package com.exam.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;
    
    private String questionType; // SINGLE_CHOICE, MULTIPLE_CHOICE, JUDGMENT, SHORT_ANSWER
    
    private String content;
    
    private String options; // JSON格式
    
    private String answer;
    
    private String analysis;
    
    private String difficulty; // EASY, MEDIUM, HARD
    
    private BigDecimal score;
    
    private String tags;
    
    private Integer status;
    
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
