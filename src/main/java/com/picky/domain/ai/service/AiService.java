package com.picky.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private String createPrompt(String questionTitle, String questionContent, List<String> comments) {
        String commentStr = String.join("\n- ", comments);

        return String.format(
            """
            아래는 책에 대한 하나의 질문과 그에 대한 여러 댓글들입니다. 전체 내용을 분석해서 다음 두 가지 작업을 수행해주세요.

            [질문 제목]
            %s

            [질문 내용]
            %s

            [댓글 목록]
            - %s

            ---

            [작업]
            1. 전체 토론 내용을 핵심만 요약해서 한 문장의 한국어로 작성해주세요.
            2. 이 토론의 핵심을 나타내는 해시태그를 3개 생성해주세요. '#'으로 시작하고 콤마(,)로 구분해주세요. (예: #토론,#서사불쌍,#무한공감)

            [출력 형식]
            반드시 아래와 같은 JSON 형식으로만 응답해주세요. 다른 설명은 추가하지 마세요.
            {
              "summary": "요약 내용",
              "hashtags": "#해시태그1,#해시태그2,#해시태그3"
            }
            """, questionTitle, questionContent, commentStr
        );
    }

    public Mono<AIResponseDTO> getSummaryAndHashtags(String questionTitle, String questionContent, List<String> comments) {
        String prompt = createPrompt(questionTitle, questionContent, comments);

        List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", prompt));
        Map<String, Object> body = Map.of(
            "model", "gpt-3.5-turbo",
            "messages", messages,
            "temperature", 0.7
        );

        return webClient.post()
                        .uri(OPENAI_API_URL)
                        .header("Authorization", "Bearer " + apiKey)
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(responseBody -> {
                            log.info("OpenAI Raw Response: {}", responseBody);

                            try {
                                JsonNode root = objectMapper.readTree(responseBody);
                                String content = root.path("choices").get(0).path("message").path("content").asText();

                                log.info("OpenAI content field: {}", content);

                                JsonNode inner = objectMapper.readTree(content);
                                String summary = inner.path("summary").asText();
                                String hashtags = inner.path("hashtags").asText();

                                log.info("Parsed summary={}, hashtags={}", summary, hashtags);

                                return Mono.just(new AIResponseDTO(summary, hashtags));

                            } catch (Exception e) {
                                log.error("Failed to parse OpenAI response: {}", responseBody, e);
                                return Mono.error(e);
                            }
                        })
                        .doOnError(error -> log.error("Error while calling OpenAI API: {}", error.getMessage()));
    }


    public record AIResponseDTO(String summary, String hashtags){}
}
