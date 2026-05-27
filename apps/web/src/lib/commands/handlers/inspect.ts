import type { CommandHandler } from "@/lib/commands/types";
import { publishInspectorLine } from "@/lib/inspector/bus";
import { useInspectorStore } from "@/stores/inspectorStore";


export const inspectHandler: CommandHandler = () => {
  const willOpen = !useInspectorStore.getState().open;
  useInspectorStore.getState().toggle();
  publishInspectorLine({
    kind: "command",
    text: `/inspect -> panel ${willOpen ? "open" : "closed"}`,
  });
};
