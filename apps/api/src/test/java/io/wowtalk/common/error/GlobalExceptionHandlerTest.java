package io.wowtalk.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.channel.service.ChannelNotFoundException;
import io.wowtalk.common.config.RequestLoggingFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void 도메인_예외는_공통_에러_응답으로_변환한다() {
        MDC.put(RequestLoggingFilter.REQUEST_ID_MDC_KEY, "request-123");

        ResponseEntity<ErrorResponse> response = handler.handleWowTalkException(new ChannelNotFoundException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                ErrorCode.CHANNEL_NOT_FOUND.name(),
                ErrorCode.CHANNEL_NOT_FOUND.message()
        ));
    }

    @Test
    void 알수없는_예외는_내부_서버_오류로_변환한다() {
        ResponseEntity<ErrorResponse> response = handler.handleException(new IllegalStateException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        ));
    }
}
