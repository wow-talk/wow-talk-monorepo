"use client";

import { useEffect, useRef } from "react";

import { MessageItem } from "@/features/chat/MessageItem";
import type { ChatMessageResult } from "@/types/api";

import * as styles from "./MessageList.css";

export function MessageList({ messages }: { messages: ChatMessageResult[] }) {
  const anchorRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    anchorRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages]);

  return (
    <ul className={styles.list}>
      {messages.map((m, i) => (
        <MessageItem key={`${m.sessionId}-${m.sentAt}-${i}`} message={m} />
      ))}
      <div ref={anchorRef} className={styles.anchor} />
    </ul>
  );
}
