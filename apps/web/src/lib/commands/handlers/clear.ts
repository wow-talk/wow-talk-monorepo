import type { CommandHandler } from "@/lib/commands/types";
import { useInspectorStore } from "@/stores/inspectorStore";


export const clearHandler: CommandHandler = () => {
  useInspectorStore.getState().clear();
};
