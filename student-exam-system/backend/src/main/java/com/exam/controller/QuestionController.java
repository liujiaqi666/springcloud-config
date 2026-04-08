package com.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.dto.Result;
import com.exam.model.Question;
import com.exam.service.QuestionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question")
public class QuestionController {
    
    private final QuestionService questionService;
    
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @GetMapping("/list")
    public Result<Page<Question>> getQuestions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String questionType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Question> page = questionService.getQuestions(courseId, questionType, pageNum, pageSize);
        return Result.success(page);
    }
    
    @GetMapping("/{id}")
    public Result<Question> getQuestion(@PathVariable Long id) {
        Question question = questionService.getQuestionById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        // 不返回答案
        question.setAnswer(null);
        question.setAnalysis(null);
        return Result.success(question);
    }
    
    @PostMapping
    public Result<Boolean> createQuestion(@RequestBody Question question) {
        try {
            boolean success = questionService.createQuestion(question);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping
    public Result<Boolean> updateQuestion(@RequestBody Question question) {
        try {
            boolean success = questionService.updateQuestion(question);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuestion(@PathVariable Long id) {
        try {
            boolean success = questionService.deleteQuestion(id);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
