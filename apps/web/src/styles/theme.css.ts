import { createGlobalTheme } from "@vanilla-extract/css";

import { tokens } from "./tokens";

/**
 * `:root`에 CSS 변수로 토큰을 펼친다.
 * 컴포넌트 스타일에서는 항상 `vars.*`로 참조해 hex/px 리터럴을 우회 금지.
 */
export const vars = createGlobalTheme(":root", tokens);
