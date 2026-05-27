"use client";

import { useCallback, useEffect } from "react";

import { ChatComposer } from "@/features/chat/ChatComposer";
import { MessageList } from "@/features/chat/MessageList";
import { SessionBadge } from "@/features/chat/SessionBadge";
import { useChatHistory } from "@/features/chat/useChatHistory";
import { useEnsureChannel } from "@/hooks/useEnsureChannel";
import { parseInput } from "@/lib/commands/parser";
import { lookupCommand } from "@/lib/commands/registry";
import { publishInspectorLine } from "@/lib/inspector/bus";
import { useSessionStore } from "@/stores/sessionStore";

import * as styles from "./ChatPanel.css";

export function ChatPanel({ roomId }: { roomId: string }) {
  const sessionId = useSessionStore((s) => s.sessionId);
  const ensure = useEnsureChannel();

  useEffect(() => {
    if (!roomId) return;
    ensure.mutate(roomId);
    // ensure.mutate 자체는 stable. roomId 변경 시에만 실행.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roomId]);

  const channelReady = ensure.isSuccess;
  const ready = ensure.isSuccess || ensure.isError;

  const chat = useChatHistory(
    roomId,
    channelReady && sessionId ? sessionId : "",
  );
  const { sendMessage, disconnectSocket } = chat;

  const handleSubmit = useCallback(
    (raw: string) => {
      const parsed = parseInput(raw);
      if (parsed.kind === "empty") return;

      if (parsed.kind === "command") {
        const cmd = lookupCommand(parsed.token);
        if (!cmd) {
          publishInspectorLine({
            kind: "system",
            text: `[unknown command] ${parsed.token}`,
          });
          return;
        }
        cmd.handler(
          {
            roomId,
            sessionId,
            disconnectSocket,
          },
          parsed.args,
        );
        return;
      }

      // text
      sendMessage(parsed.value);
    },
    [roomId, sessionId, disconnectSocket, sendMessage],
  );

  return (
    <main className={styles.layout}>
      <header className={styles.header}>
        <h1 className={styles.title}>#{roomId}</h1>
        <SessionBadge socketStatus={chat.socketStatus} />
      </header>

      {ensure.isError && (
        <div className={styles.errorLine}>
          채널 보장 실패: {ensure.error.message}
        </div>
      )}
      {chat.historyError && (
        <div className={styles.errorLine}>
          히스토리 로드 실패: {chat.historyError.message}
        </div>
      )}

      <div className={styles.messages}>
        {!ready || chat.isHistoryPending ? (
          <div className={styles.empty}>준비 중…</div>
        ) : chat.messages.length === 0 ? (
          <div className={styles.empty}>
            첫 메시지를 남겨보세요. <code>/help</code> 도 한 번 쳐보세요.
          </div>
        ) : (
          <MessageList messages={chat.messages} />
        )}
      </div>

      <ChatComposer
        disabled={!channelReady || chat.socketStatus !== "open"}
        onSubmit={handleSubmit}
      />
    </main>
  );
}
