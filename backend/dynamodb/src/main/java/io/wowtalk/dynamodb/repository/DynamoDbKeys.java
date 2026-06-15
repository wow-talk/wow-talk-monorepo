package io.wowtalk.dynamodb.repository;

import io.wowtalk.message.domain.ChatMessage;
import io.wowtalk.message.domain.MessageId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;

/**
 * wowtalk-main DynamoDB 테이블의 partition key와 sort key를 생성하는 helper다.
 *
 * <p>adapter 바깥으로 DynamoDB key prefix가 새지 않게 하려고 key 조합을 이 클래스에 모은다.
 */
public final class DynamoDbKeys {

    /*
     * 메인 테이블 access pattern:
     * - USER#<userId> / PROFILE: 게스트 또는 인증 사용자 프로필
     * - AUTH#<provider>#<providerSubject> / IDENTITY: 외부 인증 주체와 내부 user 연결
     * - ROOM#<roomId> / PROFILE: 방 제품 메타데이터
     * - ROOM#<roomId> / CHANNEL: 방 전송 방식 메타데이터
     * - ROOM#<roomId> / MEMBER#<userId>: 방 참여자 정보
     * - ROOM#<roomId> / MSG#<epochMillis>#<messageId>: 방 메시지 타임라인
     */
    public static final String PK = "pk";
    public static final String SK = "sk";

    private static final String USER_PREFIX = "USER#";
    private static final String AUTH_PREFIX = "AUTH#";
    private static final String ROOM_PREFIX = "ROOM#";
    private static final String MEMBER_PREFIX = "MEMBER#";
    static final String MESSAGE_PREFIX = "MSG#";
    private static final String USER_PROFILE_SK = "PROFILE";
    private static final String AUTH_IDENTITY_SK = "IDENTITY";
    private static final String ROOM_PROFILE_SK = "PROFILE";
    private static final String CHANNEL_SK = "CHANNEL";

    private DynamoDbKeys() {
    }

    static String userPk(UserId userId) {
        return USER_PREFIX + userId.value();
    }

    static String userSk() {
        return USER_PROFILE_SK;
    }

    static String authIdentityPk(String provider, String providerSubject) {
        return AUTH_PREFIX + provider + "#" + providerSubject;
    }

    static String authIdentitySk() {
        return AUTH_IDENTITY_SK;
    }

    static String roomPk(RoomId roomId) {
        return ROOM_PREFIX + roomId.value();
    }

    static String roomSk() {
        return ROOM_PROFILE_SK;
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
