import type { InspectorLine as InspectorLineType } from "@/types/inspector";

import * as styles from "./InspectorLine.css";

export function InspectorLine({ line }: { line: InspectorLineType }) {
  const d = new Date(line.timestamp);
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  const ss = String(d.getSeconds()).padStart(2, "0");
  return (
    <div className={styles.row}>
      <span className={styles.time}>{`${hh}:${mm}:${ss}`}</span>
      <span className={styles.kind[line.kind]}>{line.kind}</span>
      <span className={styles.text}>{line.text}</span>
    </div>
  );
}
