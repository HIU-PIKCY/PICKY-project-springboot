package com.picky.domain.question.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.answer.repository.AnswerRepository;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import com.picky.domain.question.web.dto.QuestionRequestDTO.QuestionPostRequestDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionDetailResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionInfoResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionListResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import com.picky.domain.questionLike.repository.QuestionLikeRepository;
import com.picky.domain.question.web.dto.QuestionResponseDTO.MyQuestionsResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.MyQuestionDTO;
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

    private final QuestionRepository questionRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final QuestionLikeRepository questionLikeRepository;

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
                .isAiGenerated(false)
                .member(member)
                .build();

        Question savedQuestion = questionRepository.save(question);

        return QuestionPostResponseDTO.builder()
                .id(savedQuestion.getId())
                .title(savedQuestion.getTitle())
                .content(savedQuestion.getContent())
                .page(savedQuestion.getPageNum())
                .isAI(savedQuestion.getIsAiGenerated())
                .createdAt(savedQuestion.getCreatedAt())
                .build();
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

        // 조회수 증가
        int updatedViews = questionRepository.increaseViews(questionId);
        if (updatedViews == 0) {
            throw new GeneralException(ErrorStatus.QUESTION_NOT_FOUND);
        }

        Question question = questionRepository.findByIdWithBookAndMember(questionId)
                                              .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 좋아요 여부 확인
        Boolean isLiked = questionLikeRepository.existsByMemberAndQuestion(member, question);

        return QuestionDetailResponseDTO.builder()
                .id(question.getId())
                .title(question.getTitle())
                .content(question.getContent())
                .author(question.getMember().getNickname())
                .isAI(question.getIsAiGenerated())
                .views(question.getViews())
                .likes(question.getQuestionLikes().size())
                .answersCount(question.getAnswers().size())
                .page(question.getPageNum())
                .createdAt(question.getCreatedAt())
                .book(QuestionResponseDTO.BookInfoResponseDTO.builder()
                        .id(question.getBook().getId())
                        .title(question.getBook().getTitle())
                        .author(question.getBook().getAuthor())
                        .build())
                .isLiked(isLiked)
                .build();
    }

    @Override
    public QuestionListResponseDTO getQuestionList(Long bookId) {

        bookRepository.findById(bookId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_NOT_FOUND));

        List<Question> questions = questionRepository.findByBookIdWithBook(bookId);

        List<QuestionInfoResponseDTO> questionInfoResponseDTOs = questions.stream()
                .map(q -> QuestionResponseDTO.QuestionInfoResponseDTO.builder()
                    .id(q.getId())
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
            .totalCount(questionInfoResponseDTOs.size())
            .hasMore(false) // TODO: 추후 페이징 한다면 처리
            .build();
    }
}
