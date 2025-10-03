package com.picky.domain.member.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.answer.service.AnswerService;
import com.picky.domain.answer.web.dto.AnswerResponseDTO;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.service.MemberService;
import com.picky.domain.member.web.dto.MemberResponseDTO;
import com.picky.domain.member.web.dto.MyMenuResponseDTO;
import com.picky.domain.member.web.dto.NicknameCheckResponseDTO;
import com.picky.domain.member.web.dto.PatchMemberRequestDTO;
import com.picky.domain.question.service.QuestionService;
import com.picky.domain.question.web.dto.QuestionResponseDTO;
import com.picky.domain.questionLike.service.QuestionLikeService;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/members")
@Tag(name = "사용자")
public class MemberController {
    private final MemberService memberService;
    private final AnswerService answerService;
    private final QuestionService questionService;
    private final QuestionLikeService questionLikeService;

    @Operation(summary = "내 정보 조회 API", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/profile")
    public ApiResponse<MemberResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Member currentMember = customerUserDetails.getMember();
        MemberResponseDTO memberResponseDTO = MemberResponseDTO.fromEntity(currentMember);
        return ApiResponse.onSuccess(memberResponseDTO);
    }

    @Operation(summary = "마이메뉴 조회 API", description = "마이메뉴에서 필요한 정보를 조회합니다.")
    @GetMapping("/mymenu")
    public ApiResponse<MyMenuResponseDTO> getMyMenu(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Member currentMember = customerUserDetails.getMember();
        MyMenuResponseDTO myMenuResponseDTO = memberService.getMyMenu(currentMember.getId());
        return ApiResponse.onSuccess(myMenuResponseDTO);
    }

    @Operation(summary = "내 정보 수정 API", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @PatchMapping("/profile")
    public ApiResponse<MemberResponseDTO> patchMember(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails,
            @Valid @RequestBody PatchMemberRequestDTO request
    ) {
        Member currentMember = customerUserDetails.getMember();
        MemberResponseDTO memberResponseDTO = memberService.patchMember(currentMember, request);
        return ApiResponse.onSuccess(memberResponseDTO);
    }

    @Operation(summary = "사용자 답변 목록 조회 API", description = "특정 사용자가 작성한 모든 답변 목록을 조회합니다.")
    @GetMapping("/answers")
    public ApiResponse<AnswerResponseDTO.MyAnswersResponseDTO> getMyAnswers(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Member currentMember = customerUserDetails.getMember();
        return ApiResponse.onSuccess(answerService.getMyAnswers(currentMember.getId()));
    }

    @Operation(summary = "사용자 질문 목록 조회 API", description = "특정 사용자가 작성한 모든 질문 목록을 조회합니다.")
    @GetMapping("/questions")
    public ApiResponse<QuestionResponseDTO.MyQuestionsResponseDTO> getMyQuestions(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Member currentMember = customerUserDetails.getMember();
        return ApiResponse.onSuccess(questionService.getMyQuestions(currentMember.getId()));
    }

    @Operation(summary = "사용자 좋아요 목록 조회 API", description = "특정 사용자가 좋아요한 모든 게시물 목록을 조회합니다.")
    @GetMapping("/question-likes")
    public ApiResponse<QuestionLikeResponseDTO.MyLikesResponseDTO> getMyLikes(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Member currentMember = customerUserDetails.getMember();
        return ApiResponse.onSuccess(questionLikeService.getMyLikes(currentMember.getId()));
    }

    @Operation(summary = "닉네임 중복 검사 API", description = "닉네임 중복 여부를 조회합니다.")
    @GetMapping("/check-nickname")
    public ApiResponse<NicknameCheckResponseDTO> checkNicknameDuplicate(
            @Parameter(description = "검사할 닉네임", example = "피키")
            @RequestParam String nickname
    ) {
        boolean isAvailable = memberService.isNicknameAvailable(nickname);

        NicknameCheckResponseDTO response = NicknameCheckResponseDTO.builder()
                .nickname(nickname)
                .isAvailable(isAvailable)
                .message(isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.")
                .build();

        return ApiResponse.onSuccess(response);
    }
}