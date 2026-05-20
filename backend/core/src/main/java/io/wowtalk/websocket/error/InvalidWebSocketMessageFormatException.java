package io.wowtalk.websocket.error;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

public class InvalidWebSocketMessageFormatException extends WowTalkException {

    public InvalidWebSocketMessageFormatException() {
        super(ErrorCode.INVALID_WEBSOCKET_MESSAGE_FORMAT);
    }
}
