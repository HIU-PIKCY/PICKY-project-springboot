package com.picky.domain.ai.web.controller;

import com.picky.domain.ai.service.AiService;
import com.picky.domain.ai.service.QuestionAiUpdateService;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.ai.web.dto.AiQuestionRequestDTO;
import com.picky.domain.member.entity.Member;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@Tag(name = "ai")
public class AiController {

    private final AiService aiService;
    private final QuestionAiUpdateService questionAiUpdateService;

    @PostMapping("/generate-question")
    public Mono<ResponseEntity<QuestionPostResponseDTO>> generateQuestion(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails,
            @Valid @RequestBody AiQuestionRequestDTO questionRequestDTO
    ) {
        Member currentMember = customerUserDetails.getMember();
        return aiService.generateQuestion(questionRequestDTO, currentMember)
                .map(ResponseEntity::ok);
    }
}