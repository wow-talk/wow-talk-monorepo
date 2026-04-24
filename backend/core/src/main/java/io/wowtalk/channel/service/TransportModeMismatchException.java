package io.wowtalk.channel.service;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

public class TransportModeMismatchException extends WowTalkException {

    public TransportModeMismatchException() {
        super(ErrorCode.TRANSPORT_MODE_MISMATCH);
    }
}
