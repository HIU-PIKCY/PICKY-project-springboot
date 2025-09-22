package com.picky.domain.ai.web.controller;

import com.picky.domain.ai.service.AiService;
import com.picky.domain.ai.web.dto.AiRequestDTO.AiAnswerRequestDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerCreateResponseDTO;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.ai.web.dto.AiRequestDTO.AiQuestionRequestDTO;
import com.picky.domain.member.entity.Member;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@Tag(name = "ai")
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI 질문 생성 API", description = "책과 주제에 기반한 질문을 생성합니다.")
    @PostMapping("/generate-question")
    public Mono<ResponseEntity<QuestionPostResponseDTO>> generateQuestion(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails,
            @Valid @RequestBody AiQuestionRequestDTO questionRequestDTO
    ) {
        Member currentMember = customerUserDetails.getMember();
        return aiService.generateQuestion(questionRequestDTO, currentMember)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "AI 답변 생성 API", description = "질문에 기반한 답변을 생성합니다.")
    @PostMapping("/generate-answer/{questionId}")
    public Mono<ResponseEntity<AnswerCreateResponseDTO>> generateAnswer(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails,
            @PathVariable Long questionId
    ) {
        Member currentMember = customerUserDetails.getMember();

        return aiService.generateAnswer(questionId, currentMember)
                .map(ResponseEntity::ok);
    }

}