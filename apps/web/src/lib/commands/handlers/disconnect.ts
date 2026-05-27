import { publishInspectorLine } from "@/lib/inspector/bus";

import type { CommandHandler } from "@/lib/commands/types";

export const disconnectHandler: CommandHandler = (ctx) => {
  ctx.disconnectSocket();
  publishInspectorLine({
    kind: "command",
    text: "/disconnect -> socket close, reconnect disabled",
  });
};
