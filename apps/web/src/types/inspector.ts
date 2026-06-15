/**
 * Inspector 패널에 표시되는 라인의 형식.
 *
 * 라인은 글로벌 inspectorBus를 통해 emit되고, inspectorStore가 구독해 누적한다.
 * kind별로 색상이 다르게 표시(InspectorLine 컴포넌트가 매핑).
 */

export type InspectorLineKind =
  | "status" // socket -> connecting / open / closed / error
  | "incoming" // 서버에서 받은 메시지 한 줄 요약
  | "outgoing" // 우리가 보낸 SEND_MESSAGE
  | "retry" // 재연결 시도
  | "parse-error" // Zod 실패
  | "command" // 사용자가 친 슬래시 커맨드
  | "system"; // help 출력, unknown command 등 시스템 메시지

export interface InspectorLine {
  id: string;
  timestamp: number;
  kind: InspectorLineKind;
  text: string;
}
