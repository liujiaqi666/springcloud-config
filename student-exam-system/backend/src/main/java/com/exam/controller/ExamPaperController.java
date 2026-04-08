package com.exam.controller;

import com.exam.dto.Result;
import com.exam.model.ExamPaper;
import com.exam.service.ExamPaperService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paper")
public class ExamPaperController {
    
    private final ExamPaperService examPaperService;
    
    public ExamPaperController(ExamPaperService examPaperService) {
        this.examPaperService = examPaperService;
    }
    
    @GetMapping("/list")
    public Result<List<ExamPaper>> getExamPapers(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status) {
        List<ExamPaper> papers = examPaperService.getExamPapers(courseId, status);
        return Result.success(papers);
    }
    
    @GetMapping("/{id}")
    public Result<ExamPaper> getExamPaper(@PathVariable Long id) {
        ExamPaper paper = examPaperService.getExamPaperById(id);
        if (paper == null) {
            return Result.error("试卷不存在");
        }
        return Result.success(paper);
    }
    
    @PostMapping
    public Result<Boolean> createExamPaper(@RequestBody ExamPaper examPaper) {
        try {
            boolean success = examPaperService.createExamPaper(examPaper);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping
    public Result<Boolean> updateExamPaper(@RequestBody ExamPaper examPaper) {
        try {
            boolean success = examPaperService.updateExamPaper(examPaper);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/publish")
    public Result<Boolean> publishExamPaper(@PathVariable Long id) {
        try {
            boolean success = examPaperService.publishExamPaper(id);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteExamPaper(@PathVariable Long id) {
        try {
            boolean success = examPaperService.deleteExamPaper(id);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
