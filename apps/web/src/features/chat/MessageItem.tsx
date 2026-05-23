"use client";

import { useSessionStore } from "@/stores/sessionStore";
import type { ChatMessageResult } from "@/types/api";

import * as styles from "./MessageItem.css";

export function MessageItem({ message }: { message: ChatMessageResult }) {
  const mySessionId = useSessionStore((s) => s.sessionId);
  const isMine = message.sessionId === mySessionId;
  const variant = isMine ? "mine" : "other";

  return (
    <li className={styles.rowVariants[variant]}>
      <div className={styles.bubble[variant]}>
        <div className={styles.metaVariants[variant]}>
          <span className={styles.author}>{message.sessionId}</span>
          <span className={styles.time}>{formatTime(message.sentAt)}</span>
        </div>
        <p className={styles.payload}>{message.payload}</p>
      </div>
    </li>
  );
}

function formatTime(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" });
}
