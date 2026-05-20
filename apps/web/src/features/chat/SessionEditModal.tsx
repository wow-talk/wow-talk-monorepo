"use client";

import { useState, type FormEvent } from "react";

import { useSessionStore } from "@/stores/sessionStore";

import * as styles from "./SessionEditModal.css";

export function SessionEditModal({ onClose }: { onClose: () => void }) {
  const currentSessionId = useSessionStore((s) => s.sessionId);
  const setSessionId = useSessionStore((s) => s.setSessionId);
  const [value, setValue] = useState(currentSessionId);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = value.trim();
    if (!trimmed) return;
    setSessionId(trimmed);
    onClose();
  }

  return (
    <div className={styles.backdrop} onClick={onClose} role="presentation">
      <form
        className={styles.dialog}
        onClick={(e) => e.stopPropagation()}
        onSubmit={handleSubmit}
      >
        <h2 className={styles.title}>별명 변경</h2>
        <p className={styles.description}>
          이 별명이 채팅에서 본인 식별자(sessionId)로 사용됩니다. 저장 시 WebSocket이 재연결됩니다.
        </p>
        <input
          className={styles.input}
          type="text"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          autoFocus
          aria-label="별명"
        />
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.cancelButton}
            onClick={onClose}
          >
            취소
          </button>
          <button
            type="submit"
            className={styles.saveButton}
            disabled={value.trim().length === 0 || value.trim() === currentSessionId}
          >
            저장
          </button>
        </div>
      </form>
    </div>
  );
}
