package com.picky.domain.questionLike.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.notification.entity.Notification;
import com.picky.domain.notification.entity.enums.NotificationType;
import com.picky.domain.notification.repository.NotificationRepository;
import com.picky.domain.notification.service.NotificationService;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import com.picky.domain.questionLike.entity.QuestionLike;
import com.picky.domain.questionLike.repository.QuestionLikeRepository;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.QuestionLikeStatusResponseDTO;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.MyLikesResponseDTO;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.LikeItemDTO;
import com.picky.global.util.TimeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionLikeServiceImpl implements QuestionLikeService{

    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final QuestionLikeRepository questionLikeRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Override
    public QuestionLikeStatusResponseDTO likeQuestion(Long questionId, Long memberId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        Member liker = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));


        Optional<QuestionLike> existingLike = questionLikeRepository.findByMemberAndQuestion(liker, question);

        if(existingLike.isPresent()) {
            // 이미 좋아요 했으면 취소 처리
            questionLikeRepository.delete(existingLike.get());

            int likeCounts = questionLikeRepository.countByQuestion(question);

            return QuestionLikeStatusResponseDTO.builder()
                    .isLiked(false)
                    .likeCounts(likeCounts)
                    .build();
        } else {
            // 좋아요 안했으면 추가 처리
            QuestionLike questionLike = QuestionLike.builder()
                    .question(question)
                    .member(liker)
                    .build();
            questionLikeRepository.save(questionLike);

            // 알림
            Member questionAuthor = question.getMember();
            if (!questionAuthor.getId().equals(liker.getId())) { // 본인 글에 좋아요 누르는건 알림 X
                String title = "Picky";
                String body = liker.getNickname() + "님이 회원님의 질문에 좋아요를 눌렀습니다.";
                Map<String, String> data = new HashMap<>();
                data.put("type", "QUESTION_LIKE");
                data.put("questionId", String.valueOf(questionId));
                notificationService.sendNotification(title, body, liker.getProfileImg(), questionAuthor, data);

                // DB에 알림 저장
                Notification notification = Notification.builder()
                                                        .receiver(questionAuthor)
                                                        .sender(liker)
                                                        .content(body)
                                                        .notificationType(NotificationType.QUESTION_LIKE)
                                                        .questionId(questionId)
                                                        .build();
                notificationRepository.save(notification);
            }

            int likeCounts = questionLikeRepository.countByQuestion(question);

            return QuestionLikeStatusResponseDTO.builder()
                    .isLiked(true)
                    .likeCounts(likeCounts)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MyLikesResponseDTO getMyLikes(Long memberId) {
        // 멤버 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 해당 멤버가 좋아요한 질문들을 조회 (질문, 책, 질문 작성자 정보 포함)
        List<QuestionLike> questionLikes = questionLikeRepository.findByMemberIdWithQuestionAndBookAndQuestionMember(memberId);

        if (questionLikes.isEmpty()) {
            return MyLikesResponseDTO.builder()
                    .likes(List.of())
                    .build();
        }

        // 질문 ID 목록 추출
        List<Long> questionIds = questionLikes.stream()
                .map(ql -> ql.getQuestion().getId())
                .collect(Collectors.toList());

        // 질문별 좋아요 수 조회
        List<Object[]> likeCountResults = questionRepository.countLikesByQuestionIds(questionIds);
        Map<Long, Long> likeCountMap = likeCountResults.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));

        // 질문별 답변 수 조회
        List<Object[]> answerCountResults = questionRepository.countAnswersByQuestionIds(questionIds);
        Map<Long, Long> answerCountMap = answerCountResults.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));

        // DTO로 변환
        List<LikeItemDTO> likeItems = questionLikes.stream()
                .map(questionLike -> convertToLikeItemDTO(questionLike, likeCountMap, answerCountMap))
                .collect(Collectors.toList());

        return MyLikesResponseDTO.builder()
                .likes(likeItems)
                .build();
    }

    /**
     * QuestionLike를 LikeItemDTO로 변환합니다.
     */
    private LikeItemDTO convertToLikeItemDTO(QuestionLike questionLike,
                                             Map<Long, Long> likeCountMap,
                                             Map<Long, Long> answerCountMap) {
        Question question = questionLike.getQuestion();

        return LikeItemDTO.builder()
                .id(question.getId())                           // 좋아요한 게시물 ID
                .title(question.getTitle())                     // 게시물 제목
                .author(question.getMember().getNickname())     // 작성자
                .time(TimeUtils.getRelativeTime(question.getCreatedAt())) // 질문이 작성된 시간 기준 상대시간
                .likes(Math.toIntExact(likeCountMap.getOrDefault(question.getId(), 0L))) // 좋아요 수
                .comments(Math.toIntExact(answerCountMap.getOrDefault(question.getId(), 0L))) // 답변 수
                .views(question.getViews())                     // 조회수
                .type("question")                               // 게시물 타입
                .originalId(question.getId())                   // 원본 게시물 ID
                .build();
    }
}