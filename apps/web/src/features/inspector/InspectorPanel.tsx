"use client";

import { useEffect, useRef } from "react";

import { InspectorHeader } from "@/features/inspector/InspectorHeader";
import { InspectorLine } from "@/features/inspector/InspectorLine";
import { useInspectorStore } from "@/stores/inspectorStore";

import * as styles from "./InspectorPanel.css";

export function InspectorPanel() {
  const open = useInspectorStore((s) => s.open);
  const lines = useInspectorStore((s) => s.lines);
  const anchorRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!open) return;
    anchorRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [lines, open]);

  return (
    <aside
      className={open ? styles.panel.open : styles.panel.closed}
      aria-hidden={!open}
    >
      <InspectorHeader lineCount={lines.length} />
      <div className={styles.body}>
        {lines.length === 0 ? (
          <p className={styles.empty}>
            /help 를 입력해 사용 가능한 커맨드를 확인하세요.
          </p>
        ) : (
          lines.map((line) => <InspectorLine key={line.id} line={line} />)
        )}
        <div ref={anchorRef} className={styles.anchor} />
      </div>
    </aside>
  );
}
