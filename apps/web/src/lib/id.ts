import { customAlphabet } from "nanoid";

/**
 * sessionId 생성 / localStorage 영속.
 *
 * 백엔드 인증이 없으므로 sessionId는 프론트가 정한다.
 * 중복 방지도 프론트 책임 — 같은 브라우저 같은 origin 안에서는 localStorage로 영속하되,
 * 같은 사람이 두 탭을 열면 같은 sessionId가 공유된다(데모 의도상 OK).
 */

const STORAGE_KEY = "wow-talk:sessionId";
const ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
const generate = customAlphabet(ALPHABET, 8);

export function generateSessionId(): string {
  return `user-${generate()}`;
}

export function getOrCreateSessionId(): string {
  if (typeof window === "undefined") return "";
  const existing = window.localStorage.getItem(STORAGE_KEY);
  if (existing && existing.trim()) return existing;
  const fresh = generateSessionId();
  window.localStorage.setItem(STORAGE_KEY, fresh);
  return fresh;
}

export function storeSessionId(value: string): void {
  if (typeof window === "undefined") return;
  if (!value.trim()) return;
  window.localStorage.setItem(STORAGE_KEY, value);
}
