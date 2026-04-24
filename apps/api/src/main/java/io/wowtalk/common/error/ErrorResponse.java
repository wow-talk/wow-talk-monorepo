package io.wowtalk.common.error;

public record ErrorResponse(
        String code,
        String message
) {
}
