package com.picky.domain.question.entity.enums;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum Keyword {
    // 인물
    CHARACTER_PERSONALITY("성격", "성격"),
    CHARACTER_GROWTH("성장", "성장"),
    CHARACTER_RELATIONSHIP("관계", "관계"),

    // 주제
    SUBJECT_VALUES("가치관", "가치관"),
    SUBJECT_SOCIETY("사회", "사회"),
    SUBJECT_SYMBOL("상징", "상징"),

    // 서사
    STORY_STRUCTURE("구조", "구조"),
    STORY_TECHNIQUE("기법", "기법"),

    // 지식
    KNOWLEDGE_METHOD("방법", "방법"),
    KNOWLEDGE_APPLICATION("적용/실생활/활용", "적용"); // 대표 이름은 '적용'

    private final String promptValue; // AI 프롬프트에 사용할 값
    private final String displayName; // 프론트엔드에 보여줄 값

    Keyword(String promptValue, String displayName) {
        this.promptValue = promptValue;
        this.displayName = displayName;
    }

    // AI 프롬프트 생성을 위한 헬퍼 메서드
    public static String getPromptValuesAsString() {
        return Arrays.stream(Keyword.values())
                     .map(Keyword::getPromptValue)
                     .collect(Collectors.joining(", "));
    }

    // AI 응답값으로 Enum을 찾기 위한 헬퍼 메서드
    public static Keyword fromPromptValue(String promptValue) {
        return Arrays.stream(Keyword.values())
                     .filter(k -> k.getPromptValue().equals(promptValue))
                     .findFirst()
                     .orElse(null);
    }
}
