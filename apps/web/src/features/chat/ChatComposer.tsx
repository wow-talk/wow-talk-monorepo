"use client";

import { useState, type FormEvent } from "react";

import * as styles from "./ChatComposer.css";

interface ChatComposerProps {
  disabled: boolean;
  onSend: (text: string) => boolean;
}

export function ChatComposer({ disabled, onSend }: ChatComposerProps) {
  const [text, setText] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = text.trim();
    if (!trimmed) return;
    // v0: 슬래시 커맨드 분기는 Phase 2에서. 지금은 모든 입력을 그대로 송신.
    const ok = onSend(trimmed);
    if (ok) setText("");
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <input
        className={styles.input}
        type="text"
        placeholder={disabled ? "연결 대기 중…" : "메시지를 입력하세요"}
        value={text}
        onChange={(e) => setText(e.target.value)}
        disabled={disabled}
        autoComplete="off"
      />
      <button
        className={styles.submit}
        type="submit"
        disabled={disabled || text.trim().length === 0}
      >
        보내기
      </button>
    </form>
  );
}
