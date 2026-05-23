package io.wowtalk.websocket.transport;

public enum WebSocketMessageType {
    SEND_MESSAGE,
    HELLO,
    CHAT_SEND,
    PING,
    CONNECTED,
    CHAT_MESSAGE,
    CHAT_MESSAGE_CREATED,
    ERROR
}
