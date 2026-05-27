import type { CommandHandler } from "@/lib/commands/types";
import { publishInspectorLine } from "@/lib/inspector/bus";


export const disconnectHandler: CommandHandler = (ctx) => {
  ctx.disconnectSocket();
  publishInspectorLine({
    kind: "command",
    text: "/disconnect -> socket close, reconnect disabled",
  });
};
