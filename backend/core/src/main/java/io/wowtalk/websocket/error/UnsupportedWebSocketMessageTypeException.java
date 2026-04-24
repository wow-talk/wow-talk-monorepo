package io.wowtalk.websocket.error;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

public class UnsupportedWebSocketMessageTypeException extends WowTalkException {

    public UnsupportedWebSocketMessageTypeException() {
        super(ErrorCode.UNSUPPORTED_MESSAGE_TYPE);
    }
}
