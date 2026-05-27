import { useInspectorStore } from "@/stores/inspectorStore";

import type { CommandHandler } from "@/lib/commands/types";

export const clearHandler: CommandHandler = () => {
  useInspectorStore.getState().clear();
};
