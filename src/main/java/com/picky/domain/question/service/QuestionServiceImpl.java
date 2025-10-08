package com.picky.domain.question.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.ai.service.AiService;
import com.picky.domain.ai.service.QuestionAiUpdateService;
import com.picky.domain.answer.repository.AnswerRepository;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.notification.entity.Notification;
import com.picky.domain.notification.entity.enums.NotificationType;
import com.picky.domain.notification.repository.NotificationRepository;
import com.picky.domain.notification.service.NotificationService;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import com.picky.domain.question.web.dto.QuestionRequestDTO.QuestionPostRequestDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.MyQuestionDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.MyQuestionsResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionDetailResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionInfoResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionListResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import com.picky.domain.questionLike.repository.QuestionLikeRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionServiceImpl implements QuestionService {

    private final AiService aiService;
    private final QuestionRepository questionRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final QuestionLikeRepository questionLikeRepository;
    private final QuestionAiUpdateService questionAiUpdateService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final AnswerRepository answerRepository;

    @Override
    @Transactional
    public QuestionPostResponseDTO createQuestion(Long bookId, Long memberId, QuestionPostRequestDTO request) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Question question = Question.builder()
                .book(book)
                .title(request.getTitle())
                .content(request.getContent())
                .pageNum(request.getPage())
                .isAiGenerated(request.getIsAI())
                .member(member)
                .build();

        Question savedQuestion = questionRepository.save(question);

        aiService.getKeywords(savedQuestion.getTitle(), savedQuestion.getContent())
                 // 2. 응답이 오면(비동기), 그 결과를 가지고 DB 저장 서비스를 호출
                 .subscribe(keywords ->
                     questionAiUpdateService.saveKeywords(savedQuestion.getId(), keywords)
                 );

        return new QuestionPostResponseDTO(savedQuestion);
    }

    @Override
    public MyQuestionsResponseDTO getMyQuestions(Long memberId) {
        // 멤버 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 해당 멤버가 작성한 질문들을 조회 (책 정보 포함)
        List<Question> questions = questionRepository.findByMemberIdWithBook(memberId);

        if (questions.isEmpty()) {
            return MyQuestionsResponseDTO.builder()
                    .questions(List.of())
                    .build();
        }

        // 질문 ID 목록 추출
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        // 좋아요 수 조회
        List<Object[]> likeCountResults = questionRepository.countLikesByQuestionIds(questionIds);
        Map<Long, Long> likeCountMap = likeCountResults.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));

        // 답변 수 조회
        List<Object[]> answerCountResults = questionRepository.countAnswersByQuestionIds(questionIds);
        Map<Long, Long> answerCountMap = answerCountResults.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));

        // DTO로 변환
        List<MyQuestionDTO> questionDTOs = questions.stream()
                .map(question -> convertToMyQuestionDTO(question, likeCountMap, answerCountMap))
                .collect(Collectors.toList());

        return MyQuestionsResponseDTO.builder()
                .questions(questionDTOs)
                .build();
    }

    /**
     * Question 엔티티를 MyQuestionDTO로 변환합니다.
     */
    private MyQuestionDTO convertToMyQuestionDTO(Question question,
                                                 Map<Long, Long> likeCountMap,
                                                 Map<Long, Long> answerCountMap) {
        return MyQuestionDTO.builder()
                .id(question.getId())
                .title(question.getTitle())
                .author(question.getBook().getAuthor())
                .book(question.getBook().getTitle())
                .likes(Math.toIntExact(likeCountMap.getOrDefault(question.getId(), 0L)))
                .comments(Math.toIntExact(answerCountMap.getOrDefault(question.getId(), 0L)))
                .views(question.getViews())
                .build();
    }

    @Override
    @Transactional // 조회수 증가 메서드 때문에 붙임
    public QuestionDetailResponseDTO getQuestionDetail(Long questionId, Long memberId) {

        Question question = questionRepository.findByIdWithBookAndMember(questionId)
                                              .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        // 조회수 15 달성 알림 로직
        // 현재 조회수가 14일 때만 실행 (이번 조회로 15가 되므로)
        if (question.getViews() == 14) {
            Member questionAuthor = question.getMember();

            // 본인이 본인 글을 봐서 15가 되는 경우는 알림 X
            if (!questionAuthor.getId().equals(memberId)) {
                String title = "Picky";
                String body = "회원님이 작성한 게시글이 주목받고 있어요!";
                Map<String, String> data = new HashMap<>();
                data.put("type", "VIEW_COUNT_ACHIEVED");
                data.put("questionId", String.valueOf(questionId));
                notificationService.sendNotification(title, body, null, questionAuthor, data);

                // DB에 알림 저장
                Notification notification = Notification.builder()
                                                        .member(questionAuthor)
                                                        .content(body)
                                                        .notificationType(NotificationType.VIEW_COUNT)
                                                        .questionId(questionId)
                                                        .build();
                notificationRepository.save(notification);
            }
        }

        // 조회수 증가
        questionRepository.increaseViews(questionId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Member author = question.getMember();

        // 좋아요 여부 확인
        Boolean isLiked = questionLikeRepository.existsByMemberAndQuestion(member, question);
        boolean isAuthor = author.getId().equals(memberId);

        int likeCounts = questionLikeRepository.countByQuestion(question);
        int answerCounts = answerRepository.countByQuestion(question);

        return QuestionDetailResponseDTO.builder()
                .id(question.getId())
                .profileImg(author.getProfileImg())
                .title(question.getTitle())
                .content(question.getContent())
                .authorId(author.getId())
                .author(author.getNickname())
                .isAI(question.getIsAiGenerated())
                .views(question.getViews() + 1)
                .likes(likeCounts)
                .answersCount(answerCounts)
                .page(question.getPageNum())
                .createdAt(question.getCreatedAt())
                .book(QuestionResponseDTO.BookInfoResponseDTO.builder()
                        .id(question.getBook().getId())
                        .title(question.getBook().getTitle())
                        .author(question.getBook().getAuthor())
                        .build())
                .isLiked(isLiked)
                .isAuthor(isAuthor)
                .build();
    }

    @Override
    public QuestionListResponseDTO getQuestionList(Long bookId) {

        bookRepository.findById(bookId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_NOT_FOUND));

        // 멤버까지 한 번에 가져오자
        List<Question> questions = questionRepository.findByBookIdWithMember(bookId);

        List<QuestionInfoResponseDTO> questionInfoResponseDTOs = questions.stream()
                .map(q -> QuestionResponseDTO.QuestionInfoResponseDTO.builder()
                    .id(q.getId())
                    .nickname(q.getMember().getNickname())
                    .profileImg(q.getMember().getProfileImg())
                    .title(q.getTitle())
                    .content(q.getContent())
                    .views(q.getViews())
                    .likes(q.getQuestionLikes().size())
                    .answersCount(q.getAnswers().size())
                    .isAI(q.getIsAiGenerated())
                    .page(q.getPageNum())
                    .createdAt(q.getCreatedAt())
                    .build())
            .collect(Collectors.toList());

        return QuestionListResponseDTO.builder()
            .questions(questionInfoResponseDTOs)
            .build();
    }

    @Transactional
    @Override
    public void deleteQuestion(Long questionId, Long memberId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        if (!question.getMember().getId().equals(memberId)) { // 질문 작성자만 삭제 가능
            throw new GeneralException(ErrorStatus.NOT_QUESTION_AUTHOR);
        }

        questionRepository.delete(question);
    }
}
