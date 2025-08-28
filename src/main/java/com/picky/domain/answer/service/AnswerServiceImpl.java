package com.picky.domain.answer.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.answer.entity.Answer;
import com.picky.domain.answer.repository.AnswerRepository;
import com.picky.domain.answer.web.dto.AnswerRequestDTO.AnswerCreateRequestDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerCreateResponseDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.MyAnswerDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.MyAnswersResponseDTO;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final QuestionRepository questionRepository;

    @Override
    public MyAnswersResponseDTO getMyAnswers(Long memberId) {
        // 멤버 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 해당 멤버가 작성한 답변들을 조회 (질문 및 책 정보 포함)
        List<Answer> answers = answerRepository.findByMemberIdWithQuestionAndBook(memberId);

        if (answers.isEmpty()) {
            return MyAnswersResponseDTO.builder()
                    .answers(List.of())
                    .build();
        }

        // 답변 ID 목록 추출
        List<Long> answerIds = answers.stream()
                .map(Answer::getId)
                .toList();

        // DTO로 변환
        List<MyAnswerDTO> answerDTOs = answers.stream()
                .map(this::convertToMyAnswerDTO)
                .collect(Collectors.toList());

        return MyAnswersResponseDTO.builder()
                .answers(answerDTOs)
                .build();
    }

    /**
     * Answer 엔티티를 MyAnswerDTO로 변환합니다.
     */
    private MyAnswerDTO convertToMyAnswerDTO(Answer answer) {
        return MyAnswerDTO.builder()
                .id(answer.getId())
                .title(answer.getContent())  // 답변 내용
                .questionTitle(answer.getQuestion().getTitle())  // 원본 질문 제목
                .questionId(answer.getQuestion().getId())  // 원본 질문 ID
                .views(answer.getQuestion().getViews())  // 질문의 조회수
                .build();
    }

    @Transactional
    @Override
    public AnswerCreateResponseDTO createAnswer(Long questionId, Long memberId, AnswerCreateRequestDTO request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        Answer answer = Answer.builder()
                .content(request.getContent())
                .isAiGenerated(request.getIsAI())
                .member(member)
                .question(question)
                .build();

        Answer savedAnswer = answerRepository.save(answer);

        return AnswerCreateResponseDTO.builder()
                .id(savedAnswer.getId())
                .content(savedAnswer.getContent())
                .author(member.getName())
                .isAI(savedAnswer.getIsAiGenerated())
                .createdAt(savedAnswer.getCreatedAt())
                .build();
    }
}