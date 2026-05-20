import { style, styleVariants } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

export const row = style({
  display: "flex",
});

export const rowVariants = styleVariants({
  mine: [row, { justifyContent: "flex-end" }],
  other: [row, { justifyContent: "flex-start" }],
});

const bubbleBase = style({
  maxWidth: "75%",
  padding: `${vars.space.sm} ${vars.space.md}`,
  borderRadius: vars.radius.lg,
  display: "flex",
  flexDirection: "column",
  gap: vars.space.xxs,
});

export const bubble = styleVariants({
  mine: [
    bubbleBase,
    {
      backgroundColor: vars.color.primary,
      color: vars.color.onPrimary,
      borderBottomRightRadius: vars.radius.sm,
    },
  ],
  other: [
    bubbleBase,
    {
      backgroundColor: vars.color.surfaceCard,
      color: vars.color.ink,
      borderBottomLeftRadius: vars.radius.sm,
    },
  ],
});

export const meta = style({
  display: "flex",
  gap: vars.space.xs,
  alignItems: "baseline",
  fontSize: vars.fontSize.caption,
});

export const metaVariants = styleVariants({
  mine: [meta, { color: vars.color.onPrimary, opacity: 0.85 }],
  other: [meta, { color: vars.color.muted }],
});

export const author = style({
  fontWeight: vars.fontWeight.medium,
  fontFamily: vars.font.mono,
});

export const time = style({
  fontFamily: vars.font.mono,
  fontSize: vars.fontSize.captionUppercase,
  letterSpacing: vars.letterSpacing.uppercase,
});

export const payload = style({
  margin: 0,
  fontSize: vars.fontSize.bodyMd,
  lineHeight: vars.lineHeight.body,
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
});
