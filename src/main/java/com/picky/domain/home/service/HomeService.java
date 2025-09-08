package com.picky.domain.home.service;

import com.picky.domain.home.web.dto.HomeResponseDTO.HotTopicResponseDTO;

public interface HomeService {

    /**
     * 이번 주 핫 토픽 조회
     * @return HotTopicResponseDTO 이번 주 핫 토픽 정보
     */
    HotTopicResponseDTO getHotTopic();
}
