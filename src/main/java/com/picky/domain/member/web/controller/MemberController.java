//package com.picky.domain.member.web.controller;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.picky.apiPayload.ApiResponse;
//import com.picky.domain.member.service.MemberService;
//import com.picky.domain.member.web.dto.MemberSignUpRequestDTO;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/member")
//@Tag(name = "회원")
//@Slf4j
//public class MemberController {
//
//    private final MemberService memberService;
//
//    @PostMapping("/sign-up")
//    public ApiResponse<String> signUp(@RequestBody MemberSignUpRequestDTO memberSignUpRequestDto) {
//        memberService.signUp(memberSignUpRequestDto);
//        return ApiResponse.onSuccess("회원가입에 성공했습니다.");
//    }
//}
