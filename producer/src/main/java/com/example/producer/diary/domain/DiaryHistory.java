package com.example.producer.diary.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DiaryHistory {

    private Long id;
    private String username;
    private String content;
}
