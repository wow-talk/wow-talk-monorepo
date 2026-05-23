package io.wowtalk.user.service;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

public class UserNotFoundException extends WowTalkException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
