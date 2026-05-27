import { publishInspectorLine } from "@/lib/inspector/bus";
import { useInspectorStore } from "@/stores/inspectorStore";

import type { CommandHandler } from "@/lib/commands/types";

export const inspectHandler: CommandHandler = () => {
  const willOpen = !useInspectorStore.getState().open;
  useInspectorStore.getState().toggle();
  publishInspectorLine({
    kind: "command",
    text: `/inspect -> panel ${willOpen ? "open" : "closed"}`,
  });
};
