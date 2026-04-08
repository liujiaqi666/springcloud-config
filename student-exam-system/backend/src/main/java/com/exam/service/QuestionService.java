package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.model.Question;
import com.exam.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {
    
    private final QuestionMapper questionMapper;
    
    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }
    
    public Page<Question> getQuestions(Long courseId, String questionType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Question::getCourseId, courseId);
        }
        if (questionType != null && !questionType.isEmpty()) {
            wrapper.eq(Question::getQuestionType, questionType);
        }
        wrapper.eq(Question::getStatus, 1);
        wrapper.orderByDesc(Question::getCreatedAt);
        
        return questionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
    
    public Question getQuestionById(Long id) {
        return questionMapper.selectById(id);
    }
    
    @Transactional
    public boolean createQuestion(Question question) {
        question.setStatus(1);
        return questionMapper.insert(question) > 0;
    }
    
    @Transactional
    public boolean updateQuestion(Question question) {
        return questionMapper.updateById(question) > 0;
    }
    
    @Transactional
    public boolean deleteQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question != null) {
            question.setStatus(0);
            return questionMapper.updateById(question) > 0;
        }
        return false;
    }
    
    public List<Question> getQuestionsByIds(List<Long> ids) {
        return questionMapper.selectBatchIds(ids);
    }
}
