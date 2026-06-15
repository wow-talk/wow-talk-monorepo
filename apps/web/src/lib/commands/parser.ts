/**
 * ChatComposer 입력 한 줄을 커맨드 / 텍스트 / 빈 입력으로 분류.
 *
 * 슬래시(`/`) 시작이면 무조건 커맨드 모드 — 등록 여부는 호출 측이 registry로 lookup.
 * 등록되지 않은 토큰은 호출 측에서 inspector에 unknown 라인을 출력하고 채팅 송출은 금지(헌법 8번 / 13번).
 */

export type ParsedInput =
  | { kind: "command"; token: string; args: string[] }
  | { kind: "text"; value: string }
  | { kind: "empty" };

export function parseInput(raw: string): ParsedInput {
  const trimmed = raw.trim();
  if (!trimmed) return { kind: "empty" };
  if (!trimmed.startsWith("/")) return { kind: "text", value: trimmed };
  const parts = trimmed.split(/\s+/);
  const [token, ...args] = parts;
  return { kind: "command", token: token ?? "/", args };
}
