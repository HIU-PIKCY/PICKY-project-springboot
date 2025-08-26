package com.picky.domain.questionLike.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import com.picky.domain.questionLike.entity.QuestionLike;
import com.picky.domain.questionLike.repository.QuestionLikeRepository;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.QuestionLikeStatusResponseDTO;
import java.util.Optional;
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

    @Override
    public QuestionLikeStatusResponseDTO likeQuestion(Long questionId, Long memberId) {

        Question question = questionRepository.findById(questionId)
                                   .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                                  .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));


        Optional<QuestionLike> existingLike = questionLikeRepository.findByMemberAndQuestion(member, question);

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
                                              .member(member)
                                              .build();
            questionLikeRepository.save(questionLike);

            int likeCounts = questionLikeRepository.countByQuestion(question);

            return QuestionLikeStatusResponseDTO.builder()
                .isLiked(true)
                .likeCounts(likeCounts)
                .build();
        }
    }
}
