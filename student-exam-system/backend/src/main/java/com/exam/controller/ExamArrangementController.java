package com.exam.controller;

import com.exam.dto.Result;
import com.exam.model.ExamArrangement;
import com.exam.service.ExamArrangementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/arrangement")
public class ExamArrangementController {
    
    private final ExamArrangementService arrangementService;
    
    public ExamArrangementController(ExamArrangementService arrangementService) {
        this.arrangementService = arrangementService;
    }
    
    @GetMapping("/list")
    public Result<List<ExamArrangement>> getExamArrangements(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status) {
        List<ExamArrangement> arrangements = arrangementService.getExamArrangements(classId, status);
        return Result.success(arrangements);
    }
    
    @GetMapping("/{id}")
    public Result<ExamArrangement> getExamArrangement(@PathVariable Long id) {
        ExamArrangement arrangement = arrangementService.getExamArrangementById(id);
        if (arrangement == null) {
            return Result.error("考试安排不存在");
        }
        return Result.success(arrangement);
    }
    
    @PostMapping
    public Result<Boolean> createExamArrangement(@RequestBody ExamArrangement arrangement) {
        try {
            boolean success = arrangementService.createExamArrangement(arrangement);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping
    public Result<Boolean> updateExamArrangement(@RequestBody ExamArrangement arrangement) {
        try {
            boolean success = arrangementService.updateExamArrangement(arrangement);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
