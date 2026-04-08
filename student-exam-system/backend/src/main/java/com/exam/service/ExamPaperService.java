package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.model.ExamPaper;
import com.exam.mapper.ExamPaperMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamPaperService {
    
    private final ExamPaperMapper examPaperMapper;
    
    public ExamPaperService(ExamPaperMapper examPaperMapper) {
        this.examPaperMapper = examPaperMapper;
    }
    
    public List<ExamPaper> getExamPapers(Long courseId, String status) {
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(ExamPaper::getCourseId, courseId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ExamPaper::getStatus, status);
        }
        wrapper.orderByDesc(ExamPaper::getCreatedAt);
        
        return examPaperMapper.selectList(wrapper);
    }
    
    public ExamPaper getExamPaperById(Long id) {
        return examPaperMapper.selectById(id);
    }
    
    @Transactional
    public boolean createExamPaper(ExamPaper examPaper) {
        examPaper.setStatus("DRAFT");
        return examPaperMapper.insert(examPaper) > 0;
    }
    
    @Transactional
    public boolean updateExamPaper(ExamPaper examPaper) {
        return examPaperMapper.updateById(examPaper) > 0;
    }
    
    @Transactional
    public boolean publishExamPaper(Long id) {
        ExamPaper examPaper = examPaperMapper.selectById(id);
        if (examPaper != null) {
            examPaper.setStatus("PUBLISHED");
            return examPaperMapper.updateById(examPaper) > 0;
        }
        return false;
    }
    
    @Transactional
    public boolean deleteExamPaper(Long id) {
        return examPaperMapper.deleteById(id) > 0;
    }
}
