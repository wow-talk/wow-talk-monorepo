"use client";

import { create } from "zustand";

import { subscribeInspector } from "@/lib/inspector/bus";
import type { InspectorLine } from "@/types/inspector";

/**
 * Inspector 패널의 클라이언트 상태.
 *
 * - `open`: 패널 슬라이드 인 여부
 * - `lines`: 누적 라인 (FIFO 500줄)
 *
 * 모듈 로드 시점에 inspectorBus를 subscribe해서 lines에 자동 push.
 * 이 store를 import한 어떤 모듈/컴포넌트도 별도 구독 셋업 불필요.
 */

const MAX_BUFFER = 500;

interface InspectorState {
  open: boolean;
  lines: InspectorLine[];
  toggle: () => void;
  show: () => void;
  hide: () => void;
  push: (line: InspectorLine) => void;
  clear: () => void;
}

export const useInspectorStore = create<InspectorState>((set) => ({
  open: false,
  lines: [],
  toggle: () => set((s) => ({ open: !s.open })),
  show: () => set({ open: true }),
  hide: () => set({ open: false }),
  push: (line) =>
    set((s) => {
      const next = [...s.lines, line];
      if (next.length <= MAX_BUFFER) return { lines: next };
      return { lines: next.slice(next.length - MAX_BUFFER) };
    }),
  clear: () => set({ lines: [] }),
}));

if (typeof window !== "undefined") {
  subscribeInspector((line) => {
    useInspectorStore.getState().push(line);
  });
}
