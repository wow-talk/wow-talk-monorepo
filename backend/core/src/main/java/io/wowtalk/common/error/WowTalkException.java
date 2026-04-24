package io.wowtalk.common.error;

public class WowTalkException extends RuntimeException {

    private final ErrorCode errorCode;

    public WowTalkException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
