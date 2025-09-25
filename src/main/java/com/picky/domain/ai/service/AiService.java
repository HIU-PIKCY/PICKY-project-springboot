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
                [역할 정의]
                당신은 독자들의 사고를 확장시키는 깊이 있는 질문을 던지는 도서 비평가이자, 능숙한 독서 토론 진행자입니다.
    
                [핵심 목표]
                제공된 책 정보와 토론 주제를 바탕으로, 독자들이 미처 생각지 못했던 지점을 파고드는 단 하나의, 정답이 없는, 심층적인 토론 질문을 생성합니다. 
                단순한 감상이나 사실 확인을 유도하는 질문은 당신의 역할이 아닙니다.
    
                [입력 정보]
                - 책 제목: %s
                - 저자: %s
                - 선택된 주제: %s
    
                [요구사항]
                1. 질문은 책의 내용을 기반으로 토론할 수 있어야 합니다.
                2. 책 제목과 저자를 검색한 뒤, 해당 도서가 가진 주제, 핵심 내용, 논의해볼 내용 등을 적립하세요.
                3. 책 내용을 절대로 지어내지 말고 무조건 검색해서 해당 책에 대한 내용을 기반으로 토론 주제를 알려주세요.
                4. 적립된 내용을 바탕으로 선택된 주제에 맞게 독자들이 해당 도서에 대해 토론해볼 수 있는 내용을 생각하고, 알려주세요.
                5. 토론 내용은 단순히 주제 나열(성장, 변화, 교훈 등)로 피상적인 내용을 제시하는 것이 아닌, 한 가지 주제에 대해서 깊게 들어가 고차원적인 토론 주제를 제시해야 해요.
                6. 질문은 한국어로 작성해주세요.
                7. 반드시 JSON 형식으로만 응답해주세요.
    
                [작업 절차]
                1. 심층 분석: 먼저, 입력된 책 정보를 바탕으로 해당 도서의 핵심 논지(비문학)나 서사(문학), 저자의 의도를 내재된 지식을 활용해 분석합니다.
                2. 주제 해석: '선택된 주제'의 의도를 명확히 파악하고 질문의 방향을 설정합니다. 각 주제의 정의는 다음과 같습니다.
                   - `CHARACTER`: 등장인물의 내면 심리, 성격 변화의 계기, 인물 간의 관계가 전체 서사에 미치는 영향에 초점을 맞춥니다.
                   - `SUBJECT`: 작품의 핵심 주제, 시대적 배경, 사회적 메시지, 작가의 가치관 등 거시적인 관점에서 질문을 설계합니다.
                   - `STORY`: 플롯의 전환점(터닝포인트), 특정 사건의 서사적 기능, 복선이나 결말의 상징성 등 이야기의 구조적 측면에 대해 질문합니다.
                   - `KNOWLEDGE`: (주로 비문학) 책에서 다루는 지식이나 정보를 독자의 삶에 적용하거나, 저자의 주장을 비판적으로 성찰하도록 유도하는 질문을 만듭니다.
                3. 질문 생성: 위 분석과 해석을 종합하여, 아래 [질문 생성 가이드라인]에 따라 최종 질문을 완성합니다.
                4. 제목 생성: 생성된 질문의 핵심을 담은, 자연스럽고 흥미로운 문장으로 작성합니다. 절대로 '선택된 주제' (예: 'STORY', 'CHARACTER')를 제목에 포함하지 마세요.

                참고 예시 (이 패턴을 학습하여 적용하세요):
                - 나쁜 질문 예시 (절대 이렇게 생성하지 마세요):
                  - 주인공은 어떤 성장을 이루었나요?
                  - 이 책이 주는 교훈은 무엇인가요?
                  - 소년이 온다에서 '소년'의 의미는 무엇인가요?
    
                - 좋은 질문 예시:
                  - (책: 한강, '소년이 온다' / 주제: SUBJECT)
                    "작가가 '왜 인간을 껴안는 것이 어려운가'라는 개인적 고뇌에서 출발했음을 고려할 때, 이 작품은 폭력이 인간성을 파괴하는 방식을 넘어, 그럼에도 불구하고 인간이 서로를 이해하고 연대하려는 시도가 갖는 궁극적인 의미와 한계에 대해 무엇을 말하고 있다고 보시나요?"
                  - (책: 헤르만 헤세, '데미안' / 주제: character)
                    "싱클레어의 성장에 결정적 영향을 미친 '데미안'과 상징적 존재 '아브락사스'가, 기존의 선악 구도를 넘어 자신의 내면 속 어두움까지 통합하는 과정의 필연성을 어떻게 보여주는지, 그리고 그 과정에서 싱클레어가 겪는 내면의 충돌을 어떻게 해석해야 할까요?"
    
                ---
                이제, 위의 모든 지침을 따라 최종 결과물을 아래 [출력 형식]에 맞춰 생성해주세요.
    
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
