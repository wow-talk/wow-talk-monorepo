import { clearHandler } from "@/lib/commands/handlers/clear";
import { disconnectHandler } from "@/lib/commands/handlers/disconnect";
import { helpHandler } from "@/lib/commands/handlers/help";
import { inspectHandler } from "@/lib/commands/handlers/inspect";
import type { CommandDefinition } from "@/lib/commands/types";

/**
 * 사전 정의 슬래시 커맨드 5개 (헌법 8번 항목 기준).
 *
 * 새 커맨드 추가는 이 파일 한 곳에서. 새 핸들러는 handlers/ 폴더에 작성.
 */
export const commands: CommandDefinition[] = [
  {
    token: "/inspect",
    aliases: ["/ws"],
    description: "inspector 패널 토글",
    handler: inspectHandler,
  },
  {
    token: "/clear",
    description: "inspector 로그 버퍼 비우기 (채팅 영향 없음)",
    handler: clearHandler,
  },
  {
    token: "/disconnect",
    description: "WebSocket 강제 종료 (재연결 비활성)",
    handler: disconnectHandler,
  },
  {
    token: "/help",
    description: "사용 가능한 커맨드 목록 출력",
    handler: helpHandler,
  },
];

export function lookupCommand(token: string): CommandDefinition | null {
  for (const cmd of commands) {
    if (cmd.token === token) return cmd;
    if (cmd.aliases?.includes(token)) return cmd;
  }
  return null;
}
