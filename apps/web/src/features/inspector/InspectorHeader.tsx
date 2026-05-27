"use client";

import { useInspectorStore } from "@/stores/inspectorStore";

import * as styles from "./InspectorHeader.css";

export function InspectorHeader({ lineCount }: { lineCount: number }) {
  const hide = useInspectorStore((s) => s.hide);
  return (
    <header className={styles.header}>
      <div className={styles.title}>
        <span className={styles.dot} aria-hidden />
        <span>inspector</span>
        <span className={styles.count}>[{lineCount}]</span>
      </div>
      <button
        type="button"
        className={styles.close}
        onClick={hide}
        aria-label="inspector 닫기"
      >
        ×
      </button>
    </header>
  );
}
