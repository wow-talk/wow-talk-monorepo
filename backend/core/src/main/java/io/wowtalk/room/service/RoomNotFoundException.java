package io.wowtalk.room.service;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

public class RoomNotFoundException extends WowTalkException {

    public RoomNotFoundException() {
        super(ErrorCode.ROOM_NOT_FOUND);
    }
}
