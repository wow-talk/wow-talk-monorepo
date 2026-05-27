/**
 * 슬래시 커맨드 타입 계약.
 *
 * 핸들러는 CommandContext와 args를 받아 부수효과만 일으킨다(반환값 없음).
 * 출력은 publishInspectorLine으로 inspector에. 채팅으로 송출 금지.
 */

export interface CommandContext {
  roomId: string;
  sessionId: string;
  disconnectSocket: () => void;
}

export type CommandHandler = (ctx: CommandContext, args: string[]) => void;

export interface CommandDefinition {
  token: string; // 예: "/inspect" — 슬래시 포함
  aliases?: string[]; // 동의어 토큰들 (예: ["/ws"])
  description: string;
  handler: CommandHandler;
}
