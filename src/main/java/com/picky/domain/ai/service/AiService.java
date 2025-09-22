package com.picky.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.ai.web.dto.AiRequestDTO.AiQuestionRequestDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerCreateResponseDTO;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.member.entity.Member;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final BookRepository bookRepository;
    private final QuestionAiUpdateService questionAiUpdateService;
    private final QuestionRepository questionRepository;

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


    public record AIResponseDTO(String summary, String hashtags) {
    }

    private String createQuestionPrompt(String bookTitle, String author, String theme) {
        return String.format(
                """
                당신은 독서 토론을 위한 질문 생성기입니다.
                아래 정보를 참고해서 주제에 맞는 질문 1개를 만들어주세요.
    
                [책 제목]
                %s
    
                [저자]
                %s
    
                [선택된 주제]
                %s
    
                ---
    
                [요구사항]
                1. 질문은 책의 내용을 기반으로 토론할 수 있어야 합니다.
                2. 질문은 한국어로 작성해주세요.
                3. 반드시 JSON 형식으로만 응답해주세요.
    
                [출력 형식]
                {
                  "title": "질문 제목",
                  "content": "질문 내용"
                }
                """, bookTitle, author, theme
        );
    }

    public Mono<QuestionPostResponseDTO> generateQuestion(AiQuestionRequestDTO request, Member member) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_NOT_FOUND));


        String prompt = createQuestionPrompt(book.getTitle(), book.getAuthor(), request.getQuestionType());

        List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", prompt));
        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", messages,
                "temperature", 0.8
        );

        return webClient.post()
                .uri(OPENAI_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    log.info("OpenAI Raw Response (Question): {}", responseBody);
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        String content = root.path("choices").get(0).path("message").path("content").asText();

                        JsonNode inner = objectMapper.readTree(content);
                        String title = inner.path("title").asText();
                        String questionContent = inner.path("content").asText();

                        GeneratedQuestionDTO dto = new GeneratedQuestionDTO(title, questionContent);

                        return Mono.fromCallable(() -> questionAiUpdateService.saveGeneratedQuestion(request, member, dto))
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(QuestionPostResponseDTO::new);
                    } catch (Exception e) {
                        log.error("Failed to parse generated question response: {}", responseBody, e);
                        return Mono.error(e);
                    }
                });
    }

    public record GeneratedQuestionDTO(String title, String content) {}

    private String createAnswerPrompt(String bookTitle, String author, String questionContent) {
        return String.format(
                """
                당신은 독서 토론을 위한 답변 생성기입니다.
                아래 정보를 참고하여 질문에 대한 답변을 만들어주세요.
    
                [책 제목]
                %s
    
                [저자]
                %s
    
                [질문]
                %s
    
                ---
    
                [요구사항]
                1. 답변은 책 내용을 기반으로 작성해야 합니다.
                2. 답변은 한국어로 작성해주세요.
                3. 반드시 JSON 형식으로만 응답해주세요.
    
                [출력 형식]
                {
                  "content": "답변 내용"
                }
                """, bookTitle, author, questionContent
        );
    }

    public Mono<AnswerCreateResponseDTO> generateAnswer(Long questionId, Member member) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        Book book = Optional.ofNullable(question.getBook())
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_NOT_FOUND));

        String prompt = createAnswerPrompt(
                book.getTitle(),
                book.getAuthor(),
                question.getContent()
        );

        List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", prompt));
        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", messages,
                "temperature", 0.8
        );

        return webClient.post()
                .uri(OPENAI_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    log.info("OpenAI Raw Response (Answer): {}", responseBody);
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        String content = root.path("choices").get(0).path("message").path("content").asText();

                        JsonNode inner = objectMapper.readTree(content);
                        String answerContent = inner.path("content").asText();

                        GeneratedAnswerDTO dto = new GeneratedAnswerDTO(answerContent);

                        return Mono.fromCallable(() -> questionAiUpdateService.saveGeneratedAnswer(questionId, member, dto))
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(savedAnswer -> new AnswerCreateResponseDTO(savedAnswer, member));
                    } catch (Exception e) {
                        log.error("Failed to parse generated answer response: {}", responseBody, e);
                        return Mono.error(e);
                    }
                });
    }
    public record GeneratedAnswerDTO(String content) {}

}
