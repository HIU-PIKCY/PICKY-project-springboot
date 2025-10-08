package com.picky.apiPayload.code.status;

import com.picky.apiPayload.code.BaseErrorCode;
import com.picky.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // For test
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP_4001", "이거는 테스트"),

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "금지된 요청입니다."),

    // 책
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_404", "해당 책을 찾을 수 없습니다."),
    ALREADY_ADDED(HttpStatus.BAD_REQUEST, "BOOK_400", "이미 내 서재에 추가한 책입니다."),

    // 내 서재
    BOOKSHELF_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_404", "해당 서재에 추가된 책을 찾을 수 없습니다."),

    // 멤버
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "해당 멤버를 찾을 수 없습니다."),
    NICKNAME_ALREADY_USED(HttpStatus.BAD_REQUEST, "MEMBER_400", "이미 존재하는 닉네임입니다."),

    // 질문
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_404", "해당 질문을 찾을 수 없습니다."),
    NOT_QUESTION_AUTHOR(HttpStatus.FORBIDDEN, "QUESTION_403", "질문 작성자만 삭제할 수 있습니다."),

    // 질문 좋아요
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "QUESTION_LIKE_401", "이미 좋아요를 눌렀습니다."),
    QUESTION_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_LIKE_404", "해당 질문 좋아요를 찾을 수 없습니다."),

    // 답변
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "ANSWER_404", "해당 답변을 찾을 수 없습니다."),
    INVALID_PARENT_ANSWER(HttpStatus.BAD_REQUEST, "ANSWER_400", "부모 답변이 유효하지 않습니다."),
    NOT_ANSWER_AUTHOR(HttpStatus.FORBIDDEN, "ANSWER_403", "답변 작성자만 삭제할 수 있습니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_404", "해당 알림을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
