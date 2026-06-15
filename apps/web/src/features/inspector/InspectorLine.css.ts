import { style, styleVariants } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

export const row = style({
  display: "grid",
  gridTemplateColumns: "auto 110px 1fr",
  gap: vars.space.sm,
  alignItems: "baseline",
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
  lineHeight: vars.lineHeight.code,
});

export const time = style({
  color: vars.color.onDarkSoft,
  fontSize: vars.fontSize.caption,
  fontFamily: vars.font.mono,
});

const kindBase = style({
  fontSize: vars.fontSize.captionUppercase,
  letterSpacing: vars.letterSpacing.uppercase,
  textTransform: "uppercase",
  fontFamily: vars.font.mono,
  fontWeight: vars.fontWeight.medium,
});

export const kind = styleVariants({
  status: [kindBase, { color: vars.color.accentTeal }],
  incoming: [kindBase, { color: vars.color.onDark }],
  outgoing: [kindBase, { color: vars.color.accentAmber }],
  retry: [kindBase, { color: vars.color.warning }],
  "parse-error": [kindBase, { color: vars.color.error }],
  command: [kindBase, { color: vars.color.primary }],
  system: [kindBase, { color: vars.color.onDarkSoft }],
});

export const text = style({
  color: vars.color.onDark,
  fontSize: vars.fontSize.code,
  fontFamily: vars.font.mono,
});
