package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.model.StudentExamRecord;
import com.exam.model.StudentAnswer;
import com.exam.mapper.StudentExamRecordMapper;
import com.exam.mapper.StudentAnswerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentExamRecordService {
    
    private final StudentExamRecordMapper recordMapper;
    private final StudentAnswerMapper answerMapper;
    
    public StudentExamRecordService(StudentExamRecordMapper recordMapper, StudentAnswerMapper answerMapper) {
        this.recordMapper = recordMapper;
        this.answerMapper = answerMapper;
    }
    
    public List<StudentExamRecord> getStudentRecords(Long studentId, String status) {
        LambdaQueryWrapper<StudentExamRecord> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(StudentExamRecord::getStudentId, studentId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(StudentExamRecord::getStatus, status);
        }
        wrapper.orderByDesc(StudentExamRecord::getCreatedAt);
        
        return recordMapper.selectList(wrapper);
    }
    
    public StudentExamRecord getRecordById(Long id) {
        return recordMapper.selectById(id);
    }
    
    public StudentExamRecord getRecord(Long arrangementId, Long studentId) {
        LambdaQueryWrapper<StudentExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExamRecord::getArrangementId, arrangementId);
        wrapper.eq(StudentExamRecord::getStudentId, studentId);
        return recordMapper.selectOne(wrapper);
    }
    
    @Transactional
    public boolean startExam(StudentExamRecord record) {
        record.setStartTime(LocalDateTime.now());
        record.setStatus("IN_PROGRESS");
        return recordMapper.insert(record) > 0;
    }
    
    @Transactional
    public boolean submitExam(Long recordId) {
        StudentExamRecord record = recordMapper.selectById(recordId);
        if (record != null) {
            record.setSubmitTime(LocalDateTime.now());
            record.setStatus("SUBMITTED");
            return recordMapper.updateById(record) > 0;
        }
        return false;
    }
    
    @Transactional
    public boolean saveAnswer(StudentAnswer answer) {
        StudentAnswer existing = answerMapper.selectOne(new LambdaQueryWrapper<StudentAnswer>()
            .eq(StudentAnswer::getRecordId, answer.getRecordId())
            .eq(StudentAnswer::getQuestionId, answer.getQuestionId()));
        
        if (existing != null) {
            existing.setAnswer(answer.getAnswer());
            existing.setUpdatedAt(LocalDateTime.now());
            return answerMapper.updateById(existing) > 0;
        } else {
            return answerMapper.insert(answer) > 0;
        }
    }
    
    public List<StudentAnswer> getAnswers(Long recordId) {
        LambdaQueryWrapper<StudentAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentAnswer::getRecordId, recordId);
        return answerMapper.selectList(wrapper);
    }
}
