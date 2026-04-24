package io.wowtalk.channel.service;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

public class ChannelNotFoundException extends WowTalkException {

    public ChannelNotFoundException() {
        super(ErrorCode.CHANNEL_NOT_FOUND);
    }
}
