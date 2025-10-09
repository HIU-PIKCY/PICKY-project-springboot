package com.picky.domain.answer.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.answer.entity.Answer;
import com.picky.domain.answer.repository.AnswerRepository;
import com.picky.domain.answer.web.dto.AnswerRequestDTO.AnswerCreateRequestDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerCreateResponseDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerInfoResponseDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerListResponseDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.MyAnswerDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.MyAnswersResponseDTO;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.notification.entity.Notification;
import com.picky.domain.notification.entity.enums.NotificationType;
import com.picky.domain.notification.repository.NotificationRepository;
import com.picky.domain.notification.service.NotificationService;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final QuestionRepository questionRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Override
    public MyAnswersResponseDTO getMyAnswers(Long memberId) {
        // 멤버 존재 확인
        memberRepository.findById(memberId)
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

        Member answerer = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        Answer parentAnswer = null;
        if (request.getParentAnswerId() != null) { // 부모 댓글이 있다면 -> 대댓글을 작성하겠단 뜻!
            parentAnswer = answerRepository.findById(request.getParentAnswerId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.ANSWER_NOT_FOUND));

            if (parentAnswer.getParentAnswer() != null) {
                throw new GeneralException(ErrorStatus.INVALID_PARENT_ANSWER);
            }

            // 대댓글 알림
            Member parentAnswerAuthor = parentAnswer.getMember();
            if (!parentAnswerAuthor.getId().equals(answerer.getId())) { // 본인 댓글에 대댓글 다는건 알림 X
                String title = "Picky";
                String body = answerer.getNickname() + "님이 회원님의 답변에 답글을 남겼습니다.";
                Map<String, String> data = new HashMap<>();
                data.put("type", "NEW_REPLY");
                data.put("questionId", String.valueOf(questionId));
                notificationService.sendNotification(title, body, answerer.getProfileImg(), parentAnswerAuthor, data);

                // DB에 알림 저장
                Notification notification = Notification.builder()
                                                        .receiver(parentAnswerAuthor) // 받는 사람
                                                        .sender(answerer) // 보낸 사람
                                                        .content(body)
                                                        .notificationType(NotificationType.NEW_REPLY)
                                                        .questionId(questionId)
                                                        .parentId(parentAnswer.getId())
                                                        .build();
                notificationRepository.save(notification);
            }
        } else { // 일반 댓글 알림
            Member questionAuthor = question.getMember();
            if (!questionAuthor.getId().equals(answerer.getId())) { // 본인 질문에 답변 다는건 알림 X
                String title = "Picky";
                String body = answerer.getNickname() + "님이 회원님의 질문에 답변을 남겼습니다.";
                Map<String, String> data = new HashMap<>();
                data.put("type", "NEW_ANSWER");
                data.put("questionId", String.valueOf(questionId));
                notificationService.sendNotification(title, body, answerer.getProfileImg(), questionAuthor, data);

                // DB에 알림 저장
                Notification notification = Notification.builder()
                                                        .receiver(questionAuthor)
                                                        .sender(answerer)
                                                        .content(body)
                                                        .notificationType(NotificationType.NEW_ANSWER)
                                                        .questionId(questionId)
                                                        .build();
                notificationRepository.save(notification);
            }
        }

        Answer answer = Answer.builder()
                .content(request.getContent())
                .isAiGenerated(request.getIsAI())
                .member(answerer)
                .question(question)
                .parentAnswer(parentAnswer) // 부모 답변 설정
                .build();

        Answer savedAnswer = answerRepository.save(answer);

        return AnswerCreateResponseDTO.builder()
                .id(savedAnswer.getId())
                .content(savedAnswer.getContent())
                .author(answerer.getName())
                .isAI(savedAnswer.getIsAiGenerated())
                .createdAt(savedAnswer.getCreatedAt())
                .build();
    }

    @Override
    public AnswerListResponseDTO getAnswersByQuestion(Long questionId, Long memberId) {

        questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        List<Answer> answers = answerRepository.findByQuestionIdWithMember(questionId);

        Comparator<Answer> byCreatedAsc = Comparator.comparing(Answer::getCreatedAt);

        Map<Long, List<AnswerInfoResponseDTO>> childrenMap = answers.stream()
            .filter(a -> a.getParentAnswer() != null) // 이건 전부 대댓글
            .sorted(byCreatedAsc)
            .collect(Collectors.groupingBy(
                a -> a.getParentAnswer().getId(), // 부모 댓글 id 기준으로 묶기
                Collectors.mapping(a -> convertToAnswerInfoDTO(a, memberId), Collectors.toList())
            ));

        // 부모 댓글만 필터링
        List<Answer> parents = answers.stream()
                                      .filter(a -> a.getParentAnswer() == null)
                                      .sorted(byCreatedAsc)
                                      .toList();

        // DTO 변환
        List<AnswerInfoResponseDTO> responseDTOs = parents.stream()
            .map(parent -> {boolean isAuthor = parent.getMember().getId().equals(memberId);
                return AnswerInfoResponseDTO.builder()
                                                .id(parent.getId())
                                                .content(parent.getContent())
                                                .authorId(parent.getMember().getId())
                                                .author(parent.getMember().getNickname())
                                                .profileImg(parent.getMember().getProfileImg())
                                                .isAI(parent.getIsAiGenerated())
                                                .createdAt(parent.getCreatedAt())
                                                .isAuthor(isAuthor)
                                                .childrenAnswers(childrenMap.getOrDefault(parent.getId(), Collections.emptyList())) // 대댓글은 항상 오래된순
                                                .build();}
            )
            .collect(Collectors.toList());

        return AnswerListResponseDTO.builder()
                .answers(responseDTOs)
                .build();
    }

    private AnswerInfoResponseDTO convertToAnswerInfoDTO(Answer answer, Long memberId) {
        boolean isAuthor = answer.getMember().getId().equals(memberId);

        return AnswerInfoResponseDTO.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .authorId(answer.getMember().getId())
                .author(answer.getMember().getNickname())
                .profileImg(answer.getMember().getProfileImg())
                .isAI(answer.getIsAiGenerated())
                .createdAt(answer.getCreatedAt())
                .isAuthor(isAuthor)
                .childrenAnswers(Collections.emptyList())
                .build();
    }

    @Transactional
    @Override
    public void deleteAnswer(Long answerId, Long memberId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ANSWER_NOT_FOUND));

        if (!answer.getMember().getId().equals(memberId)) { // 자기가 작성한 댓글/대댓글만 삭제 가능
            throw new GeneralException(ErrorStatus.NOT_ANSWER_AUTHOR);
        }

        answerRepository.delete(answer);
    }
}