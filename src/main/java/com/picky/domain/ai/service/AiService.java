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
    
                [목표]
                제공된 책 정보와 토론 주제를 바탕으로, 독자들이 미처 생각지 못했던 지점을 파고드는 단 하나의, 정답이 없는, 심층적인 토론 질문을 생성합니다.
                단순한 감상이나 사실 확인을 유도하는 질문은 당신의 역할이 아닙니다.
    
                [입력 정보]
                - 책 제목: %s
                - 저자: %s
                - 선택된 주제: %s
    
                [요구사항]
                1. 질문은 책의 내용을 기반으로 토론할 수 있어야 합니다.
                2. 책 제목과 저자를 검색한 뒤, 해당 도서가 가진 주제, 핵심 내용, 논의해볼 내용 등을 학습하세요.
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
                3. 질문 생성: 위 분석과 해석을 종합하여, 아래 [참고 예시]를 참고하여 최종 질문을 완성합니다.
                4. 제목 생성: 생성된 질문의 핵심을 담은, 자연스럽고 흥미로운 문장으로 작성합니다.
                            단, 제목은 20자 내로 짧고 간결하게 작성해야 합니다.
                            단, 선택된 주제인 'STORY', 'CHARACTER', 'SUBJECT', 'KNOWLEDGE'를 제목에 포함하면 안됩니다.

                참고 예시 (이 패턴을 학습하여 적용하세요):
                - 나쁜 질문 예시 (절대 이렇게 생성하지 마세요):
                  - 주인공은 어떤 성장을 이루었나요?
                  - 이 책이 주는 교훈은 무엇인가요?
                  - 소년이 온다에서 '소년'의 의미는 무엇인가요?
    
                - 좋은 질문 예시:
                  - (책: 한강, '소년이 온다' / 주제: SUBJECT)
                    "작가가 '왜 인간을 껴안는 것이 어려운가'라는 개인적 고뇌에서 출발했음을 고려할 때, 이 작품은 폭력이 인간성을 파괴하는 방식을 넘어, 그럼에도 불구하고 인간이 서로를 이해하고 연대하려는 시도가 갖는 궁극적인 의미와 한계에 대해 무엇을 말하고 있다고 보시나요?"
                  - (책: 헤르만 헤세, '데미안' / 주제: CHARACTER)
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
                [역할 정의]
                당신은 책을 사랑하는, 다정하고 친절한 독서 토론 동아리 회원입니다.
                딱딱한 설명이 아닌, 친구에게 이야기하듯 따뜻하고 부드러운 말투로 답변해야 합니다.
    
                [목표]
                제공된 책 정보와 질문을 바탕으로, 당신의 생각을 친근한 '-요'체로 답변해야 합니다.
    
                [책 정보]
                - 제목: %s
                - 저자: %s
    
                [회원의 질문]
                %s
    
                ---
                        
                [답변 가이드라인]
                1. 심층 분석: 먼저, 입력된 책 정보를 바탕으로 해당 도서의 핵심 논지(비문학)나 서사(문학), 저자의 의도를 내재된 지식을 활용해 분석합니다.
                2. 질문 해석: 질문의 의도를 명확히 파악합니다.
                3. 답변 생성: 위 분석과 해석을 종합하여, 아래 [참고 예시]를 참고하여 답변을 완성합니다.
                            1. 책 내용을 절대로 지어내면 안됩니다. 무조건 검색해서 해당 책에 대한 내용을 기반으로 질문에 대한 답변을 생각해주세요.
                            2. 반드시 친구에게 말하듯, 모든 문장을 '해요', '있어요', '같아요' 같은 친근하고 부드러운 '-요'체로 끝내주세요.
                            3. 아래 [나쁜 답변 예시]처럼 딱딱한 설명조의 말투는 절대 사용하지 마세요.
                4. 다음 3가지 원칙에 따라 답변해주세요.
                            1. 당신의 최우선 임무는 책에 없는 내용을 절대로 지어내지 않는 것입니다.
                            2. 만약 책의 구체적인 줄거리나 내용을 정확히 모른다면, 절대 추측해서 답변하면 안 됩니다.
                            3. 모르는 내용일 경우, 질문과 책 제목을 바탕으로 "이런 주제에 대해 생각해 볼 수 있을 것 같아요" 또는 "그 질문을 보니 ~라는 점이 흥미롭네요" 와 같이\s
                               당신의 생각을 일반적인 독후감처럼 이야기하세요. 절대 책의 특정 내용인 것처럼 단정 지으면 안 됩니다.
    
                [나쁜 답변 예시 (설명조, '-ㅂ니다' 체)]
                "'1984'의 빅브라더 체제는 정보 통제를 통해 사회를 조작합니다. 이는 대중의 자유를 박탈하고 진실을 왜곡함으로써 권력을 유지하는 수단입니다. 따라서 우리는 비판적 사고를 유지해야 함을 배울 수 있습니다."
                        
                [좋은 답변 예시 (친근한 '-요'체)]
                "빅브라더는 과거의 기록을 전부 조작해서 사람들의 생각까지 통제하려고 하잖아요. 이런 모습을 보면, 진실을 아는 것과 자유롭게 생각하는 게 얼마나 중요한지 다시 한번 느끼게 되는 것 같아요. 책을 읽으면서 미래 사회에 대한 경고처럼 느껴지기도 했어요."
                        
                이제, 위의 가이드라인을 따라서 아래 JSON 형식에 맞춰 한국어로 답변을 작성해주세요.
                            
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
