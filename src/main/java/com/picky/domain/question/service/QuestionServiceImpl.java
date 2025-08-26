package com.picky.domain.question.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import com.picky.domain.question.web.dto.QuestionRequestDTO.QuestionPostRequestDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionDetailResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import com.picky.domain.questionLike.repository.QuestionLikeRepository;
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
    public QuestionDetailResponseDTO getQuestionDetail(Long questionId, Long memberId) {

        Question question = questionRepository.findWithBookById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        // 좋아요 여부 확인
        Boolean isLiked = questionLikeRepository.existsByMemberIdAndQuestionId(memberId, questionId);

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
}
