package io.wowtalk.message.service;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

public class InvalidChatMessageException extends WowTalkException {

    public InvalidChatMessageException() {
        super(ErrorCode.INVALID_CHAT_MESSAGE);
    }
}
