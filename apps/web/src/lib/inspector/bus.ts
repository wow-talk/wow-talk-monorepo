import type { InspectorLine, InspectorLineKind } from "@/types/inspector";

/**
 * Inspector 라인 글로벌 emit-subscribe 버스.
 *
 * 모듈 스코프 listener set을 단일 진실로 둔다.
 * inspectorStore가 모듈 로드 시점에 subscribe해서 lines로 누적하는 sink 역할을 함.
 * 다른 출처(WsClient log, 슬래시 커맨드 핸들러)는 publishInspectorLine만 호출하면 됨 — store에 직접 접근하지 않음.
 */

type Listener = (line: InspectorLine) => void;

const listeners = new Set<Listener>();

let counter = 0;
const nextId = (): string => `il-${Date.now().toString(36)}-${++counter}`;

export interface PublishInput {
  kind: InspectorLineKind;
  text: string;
}

export function publishInspectorLine(input: PublishInput): void {
  const line: InspectorLine = {
    id: nextId(),
    timestamp: Date.now(),
    kind: input.kind,
    text: input.text,
  };
  for (const listener of listeners) listener(line);
}

export function subscribeInspector(listener: Listener): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}
