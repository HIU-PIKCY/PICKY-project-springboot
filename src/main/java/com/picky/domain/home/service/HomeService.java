package com.picky.domain.home.service;

import com.picky.domain.home.web.dto.HomeResponseDTO.HotTopicResponseDTO;
import com.picky.domain.home.web.dto.HomeResponseDTO.MostQuestionedBookResponseDTO;
import java.util.List;

public interface HomeService {

    /**
     * 이번 주 핫 토픽 조회
     * @return HotTopicResponseDTO 이번 주 핫 토픽 정보
     */
    HotTopicResponseDTO getHotTopic();

    /**
     * 가장 많이 질문된 책 목록 조회
     * @return List<MostQuestionedBookResponseDTO> 가장 많이 질문된 책 7권 정보 리스트
     */
     List<MostQuestionedBookResponseDTO> getMostQuestionedBooks();
}
