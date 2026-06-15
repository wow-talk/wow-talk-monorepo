"use client";

import { useState } from "react";

import { SessionEditModal } from "@/features/chat/SessionEditModal";
import type { WsStatus } from "@/lib/ws/wsClient";
import { useSessionStore } from "@/stores/sessionStore";

import * as styles from "./SessionBadge.css";

interface SessionBadgeProps {
  socketStatus: WsStatus;
}

export function SessionBadge({ socketStatus }: SessionBadgeProps) {
  const sessionId = useSessionStore((s) => s.sessionId);
  const [open, setOpen] = useState(false);

  return (
    <>
      <button
        type="button"
        className={styles.badge}
        onClick={() => setOpen(true)}
        aria-label="별명 변경"
      >
        <span className={styles.dot[socketStatus]} aria-hidden />
        <span>{sessionId || "…"}</span>
      </button>
      {open && <SessionEditModal onClose={() => setOpen(false)} />}
    </>
  );
}
