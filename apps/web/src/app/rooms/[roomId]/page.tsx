"use client";

import { use, useEffect } from "react";

import { ChatPanel } from "@/features/chat/ChatPanel";
import { useSessionStore } from "@/stores/sessionStore";

export default function RoomPage({
  params,
}: {
  params: Promise<{ roomId: string }>;
}) {
  const { roomId } = use(params);
  const hydrated = useSessionStore((s) => s.hydrated);
  const hydrate = useSessionStore((s) => s.hydrate);

  useEffect(() => {
    if (!hydrated) hydrate();
  }, [hydrate, hydrated]);

  if (!hydrated) return null;
  return <ChatPanel roomId={roomId} />;
}
