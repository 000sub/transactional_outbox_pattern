package com.example.producer.diary.controller;

import com.example.producer.diary.domain.Diary;
import com.example.producer.diary.domain.request.DiaryCreateRequest;
import com.example.producer.diary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/diary")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<Void> createDiary(@Valid @RequestBody DiaryCreateRequest diaryCreateRequest) {
        diaryService.create(diaryCreateRequest.getUsername(), diaryCreateRequest.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{diaryId}")
    public Diary findDiary(@PathVariable Long diaryId) {
        return diaryService.findDiary(diaryId);
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.noContent().build();
    }

}
