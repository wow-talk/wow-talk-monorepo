"use client";

import { useState, type FormEvent } from "react";

import * as styles from "./ChatComposer.css";

interface ChatComposerProps {
  disabled: boolean;
  onSubmit: (raw: string) => void;
}

export function ChatComposer({ disabled, onSubmit }: ChatComposerProps) {
  const [text, setText] = useState("");
  const startsWithSlash = text.trim().startsWith("/");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = text.trim();
    if (!trimmed) return;
    onSubmit(trimmed);
    setText("");
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <input
        className={styles.input}
        type="text"
        placeholder={
          disabled
            ? "연결 대기 중…"
            : startsWithSlash
              ? "command mode — /help 로 목록"
              : "메시지를 입력하세요"
        }
        value={text}
        onChange={(e) => setText(e.target.value)}
        disabled={disabled && !startsWithSlash}
        autoComplete="off"
      />
      <button
        className={styles.submit}
        type="submit"
        disabled={text.trim().length === 0}
      >
        {startsWithSlash ? "실행" : "보내기"}
      </button>
    </form>
  );
}
