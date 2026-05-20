"use client";

import { create } from "zustand";

import { getOrCreateSessionId, storeSessionId } from "@/lib/id";

/**
 * sessionId 클라이언트 상태.
 *
 * localStorage 접근이 SSR에서 안전하지 않으므로 store 초기값은 빈 문자열로 두고,
 * hydrate() effect를 통해 클라이언트에서 한 번 적재한다.
 */

interface SessionState {
  sessionId: string;
  hydrated: boolean;
  hydrate: () => void;
  setSessionId: (value: string) => void;
}

export const useSessionStore = create<SessionState>((set) => ({
  sessionId: "",
  hydrated: false,
  hydrate: () => {
    const id = getOrCreateSessionId();
    set({ sessionId: id, hydrated: true });
  },
  setSessionId: (value: string) => {
    const trimmed = value.trim();
    if (!trimmed) return;
    storeSessionId(trimmed);
    set({ sessionId: trimmed });
  },
}));
