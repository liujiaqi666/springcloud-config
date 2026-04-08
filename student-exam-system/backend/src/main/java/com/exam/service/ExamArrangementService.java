package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.model.ExamArrangement;
import com.exam.mapper.ExamArrangementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamArrangementService {
    
    private final ExamArrangementMapper examArrangementMapper;
    
    public ExamArrangementService(ExamArrangementMapper examArrangementMapper) {
        this.examArrangementMapper = examArrangementMapper;
    }
    
    public List<ExamArrangement> getExamArrangements(Long classId, String status) {
        LambdaQueryWrapper<ExamArrangement> wrapper = new LambdaQueryWrapper<>();
        if (classId != null) {
            wrapper.eq(ExamArrangement::getClassId, classId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ExamArrangement::getStatus, status);
        }
        wrapper.ge(ExamArrangement::getStartTime, LocalDateTime.now().minusDays(30));
        wrapper.orderByDesc(ExamArrangement::getStartTime);
        
        return examArrangementMapper.selectList(wrapper);
    }
    
    public ExamArrangement getExamArrangementById(Long id) {
        return examArrangementMapper.selectById(id);
    }
    
    @Transactional
    public boolean createExamArrangement(ExamArrangement arrangement) {
        arrangement.setStatus("SCHEDULED");
        return examArrangementMapper.insert(arrangement) > 0;
    }
    
    @Transactional
    public boolean updateExamArrangement(ExamArrangement arrangement) {
        return examArrangementMapper.updateById(arrangement) > 0;
    }
    
    @Transactional
    public boolean startExam(Long id) {
        ExamArrangement arrangement = examArrangementMapper.selectById(id);
        if (arrangement != null) {
            arrangement.setStatus("ONGOING");
            return examArrangementMapper.updateById(arrangement) > 0;
        }
        return false;
    }
    
    @Transactional
    public boolean completeExam(Long id) {
        ExamArrangement arrangement = examArrangementMapper.selectById(id);
        if (arrangement != null) {
            arrangement.setStatus("COMPLETED");
            return examArrangementMapper.updateById(arrangement) > 0;
        }
        return false;
    }
}
