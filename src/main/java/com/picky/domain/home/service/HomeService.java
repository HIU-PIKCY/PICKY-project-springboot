package com.picky.domain.home.service;

import com.picky.domain.home.web.dto.HomeResponseDTO.HotTopicResponseDTO;
import com.picky.domain.home.web.dto.HomeResponseDTO.MostQuestionedBooksResponseDTO;
import com.picky.domain.home.web.dto.HomeResponseDTO.WeeklyKeywordResponseDTO;

public interface HomeService {

    /**
     * 이번 주 핫 토픽 조회
     * @return HotTopicResponseDTO 이번 주 핫 토픽 정보
     */
    HotTopicResponseDTO getHotTopic();

    /**
     * 가장 많이 질문된 책 목록 조회
     * @return List<MostQuestionedBooksResponseDTO> 가장 많이 질문된 책 7권 정보 리스트
     */
     MostQuestionedBooksResponseDTO getMostQuestionedBooks();

     /**
      * 이번 주 키워드 TOP 3 조회
      * @return WeeklyKeywordResponseDTO 이번 주 키워드 TOP 3 정보
      */
    WeeklyKeywordResponseDTO getWeeklyTopKeywords();
}
