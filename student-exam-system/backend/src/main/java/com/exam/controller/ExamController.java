package com.exam.controller;

import com.exam.dto.Result;
import com.exam.model.StudentExamRecord;
import com.exam.model.StudentAnswer;
import com.exam.service.StudentExamRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamController {
    
    private final StudentExamRecordService recordService;
    
    public ExamController(StudentExamRecordService recordService) {
        this.recordService = recordService;
    }
    
    @GetMapping("/records")
    public Result<List<StudentExamRecord>> getRecords(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String status) {
        List<StudentExamRecord> records = recordService.getStudentRecords(studentId, status);
        return Result.success(records);
    }
    
    @PostMapping("/start")
    public Result<Boolean> startExam(@RequestBody StudentExamRecord record) {
        try {
            boolean success = recordService.startExam(record);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/submit/{recordId}")
    public Result<Boolean> submitExam(@PathVariable Long recordId) {
        try {
            boolean success = recordService.submitExam(recordId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/answer")
    public Result<Boolean> saveAnswer(@RequestBody StudentAnswer answer) {
        try {
            boolean success = recordService.saveAnswer(answer);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/answers/{recordId}")
    public Result<List<StudentAnswer>> getAnswers(@PathVariable Long recordId) {
        List<StudentAnswer> answers = recordService.getAnswers(recordId);
        return Result.success(answers);
    }
}
