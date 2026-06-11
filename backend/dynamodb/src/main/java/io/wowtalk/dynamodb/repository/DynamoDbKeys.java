package io.wowtalk.dynamodb.repository;

import io.wowtalk.message.domain.ChatMessage;
import io.wowtalk.message.domain.MessageId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;

public final class DynamoDbKeys {

    /*
     * Main table access pattern:
     * - USER#<userId> / PROFILE: guest or authenticated user profile
     * - ROOM#<roomId> / CHANNEL: room transport metadata
     * - ROOM#<roomId> / MEMBER#<userId>: room membership
     * - ROOM#<roomId> / MSG#<epochMillis>#<messageId>: room message timeline
     */
    public static final String PK = "pk";
    public static final String SK = "sk";

    private static final String USER_PREFIX = "USER#";
    private static final String ROOM_PREFIX = "ROOM#";
    private static final String MEMBER_PREFIX = "MEMBER#";
    static final String MESSAGE_PREFIX = "MSG#";
    private static final String USER_PROFILE_SK = "PROFILE";
    private static final String CHANNEL_SK = "CHANNEL";

    private DynamoDbKeys() {
    }

    static String userPk(UserId userId) {
        return USER_PREFIX + userId.value();
    }

    static String userSk() {
        return USER_PROFILE_SK;
    }

    static String roomPk(RoomId roomId) {
        return ROOM_PREFIX + roomId.value();
    }

    static String channelSk() {
        return CHANNEL_SK;
    }

    static String memberSk(UserId userId) {
        return MEMBER_PREFIX + userId.value();
    }

    static String messageSk(ChatMessage chatMessage) {
        return messageSk(chatMessage.sentAt().toEpochMilli(), chatMessage.messageId());
    }

    static String messageSk(long epochMillis, MessageId messageId) {
        return MESSAGE_PREFIX + epochMillis + "#" + messageId.value();
    }
}
