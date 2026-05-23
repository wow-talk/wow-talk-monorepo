/**
 * 도메인 식별자/enum.
 *
 * 외부 의존성 0. react/next 어떤 것도 import하지 않음.
 * 추후 모노레포 전환 시 packages/shared로 이전할 1순위 모듈.
 *
 * 백엔드 wowtalk-transport 모듈의 RoomId / SessionId / TransportMode와 1:1 대응.
 */

export type RoomId = string;
export type SessionId = string;

export type TransportMode = "WEBSOCKET" | "RAW_TCP";
