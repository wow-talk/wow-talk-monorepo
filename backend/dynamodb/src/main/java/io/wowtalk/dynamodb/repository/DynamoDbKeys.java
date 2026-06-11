package io.wowtalk.dynamodb.repository;

import io.wowtalk.message.domain.ChatMessage;
import io.wowtalk.message.domain.MessageId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;

public final class DynamoDbKeys {

    public static final String PK = "pk";
    public static final String SK = "sk";

    private DynamoDbKeys() {
    }

    static String userPk(UserId userId) {
        return "USER#" + userId.value();
    }

    static String userSk() {
        return "PROFILE";
    }

    static String roomPk(RoomId roomId) {
        return "ROOM#" + roomId.value();
    }

    static String channelSk() {
        return "CHANNEL";
    }

    static String memberSk(UserId userId) {
        return "MEMBER#" + userId.value();
    }

    static String messageSk(ChatMessage chatMessage) {
        return messageSk(chatMessage.sentAt().toEpochMilli(), chatMessage.messageId());
    }

    static String messageSk(long epochMillis, MessageId messageId) {
        return "MSG#" + epochMillis + "#" + messageId.value();
    }
}
