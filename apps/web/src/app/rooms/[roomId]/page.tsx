"use client";

import { use, useEffect } from "react";

import { ChatPanel } from "@/features/chat/ChatPanel";
import { InspectorPanel } from "@/features/inspector/InspectorPanel";
import { useInspectorStore } from "@/stores/inspectorStore";
import { useSessionStore } from "@/stores/sessionStore";

import * as styles from "./page.css";

export default function RoomPage({
  params,
}: {
  params: Promise<{ roomId: string }>;
}) {
  const { roomId } = use(params);
  const hydrated = useSessionStore((s) => s.hydrated);
  const hydrate = useSessionStore((s) => s.hydrate);
  const inspectorOpen = useInspectorStore((s) => s.open);

  useEffect(() => {
    if (!hydrated) hydrate();
  }, [hydrate, hydrated]);

  if (!hydrated) return null;

  return (
    <div className={inspectorOpen ? styles.shell.open : styles.shell.closed}>
      <ChatPanel roomId={roomId} />
      <InspectorPanel />
    </div>
  );
}
