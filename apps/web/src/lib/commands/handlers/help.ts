import { commands } from "@/lib/commands/registry";
import type { CommandHandler } from "@/lib/commands/types";
import { publishInspectorLine } from "@/lib/inspector/bus";


export const helpHandler: CommandHandler = () => {
  publishInspectorLine({ kind: "system", text: "available commands:" });
  for (const cmd of commands) {
    const aliasPart = cmd.aliases?.length
      ? ` (alias: ${cmd.aliases.join(", ")})`
      : "";
    publishInspectorLine({
      kind: "system",
      text: `  ${cmd.token}${aliasPart} — ${cmd.description}`,
    });
  }
};
