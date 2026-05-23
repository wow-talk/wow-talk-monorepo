import { globalStyle } from "@vanilla-extract/css";

import { vars } from "./theme.css";

/**
 * 글로벌 reset + body 기본값.
 * 나머지 스타일은 컴포넌트 로컬 `*.css.ts`에서.
 */

globalStyle("*, *::before, *::after", {
  boxSizing: "border-box",
  margin: 0,
  padding: 0,
});

globalStyle("html, body", {
  height: "100%",
});

globalStyle("body", {
  backgroundColor: vars.color.canvas,
  color: vars.color.ink,
  fontFamily: vars.font.sans,
  fontSize: vars.fontSize.bodyMd,
  lineHeight: vars.lineHeight.body,
  WebkitFontSmoothing: "antialiased",
  MozOsxFontSmoothing: "grayscale",
});

globalStyle("h1, h2, h3, h4, h5, h6", {
  fontFamily: vars.font.serif,
  fontWeight: vars.fontWeight.regular,
});

globalStyle("a", {
  color: "inherit",
  textDecoration: "none",
});

globalStyle("code, pre, kbd, samp", {
  fontFamily: vars.font.mono,
});

globalStyle("button, input, textarea, select", {
  font: "inherit",
  color: "inherit",
});

globalStyle("button", {
  cursor: "pointer",
  background: "transparent",
  border: "none",
});
