package com.example.producer.diary.service;

import com.example.producer.diary.domain.Diary;
import com.example.producer.diary.domain.DiaryHistory;
import com.example.producer.diary.repository.DiaryRepository;
import com.example.producer.outbox.domain.OutboxMessage;
import com.example.producer.outbox.repository.OutboxMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void create(String username, String content) {
        Diary diary = Diary.of(username, content);
        log.info("create new Diary = {}", diary);
        diaryRepository.save(diary);
    }

    public Diary findDiary(Long diaryId) {
        return diaryRepository.findById(diaryId)
                .orElseThrow();
    }

    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow();

        DiaryHistory diaryHistory = new DiaryHistory(diaryId, diary.getUsername(), diary.getContent());
        eventPublisher.publishEvent(diaryHistory);

        try {
            String payload = objectMapper.writeValueAsString(diaryHistory);
            OutboxMessage message = OutboxMessage.of(payload);
            outboxMessageRepository.save(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        diaryRepository.delete(diary);
    }
}
