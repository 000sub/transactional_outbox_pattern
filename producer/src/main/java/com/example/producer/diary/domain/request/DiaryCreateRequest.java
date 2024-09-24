package com.example.producer.diary.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DiaryCreateRequest {

    @NotBlank
    @Size(max = 20)
    private String username;
    
    @NotBlank
    private String content;
}
