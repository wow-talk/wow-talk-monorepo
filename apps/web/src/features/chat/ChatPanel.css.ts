import { style } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

export const layout = style({
  display: "flex",
  flexDirection: "column",
  height: "100dvh",
  maxWidth: "720px",
  margin: "0 auto",
  padding: vars.space.lg,
  gap: vars.space.md,
});

export const header = style({
  display: "flex",
  justifyContent: "space-between",
  alignItems: "baseline",
  paddingBlock: vars.space.sm,
  borderBottom: `1px solid ${vars.color.hairline}`,
});

export const title = style({
  fontFamily: vars.font.serif,
  fontSize: vars.fontSize.displaySm,
  fontWeight: vars.fontWeight.regular,
  letterSpacing: vars.letterSpacing.displaySm,
  color: vars.color.ink,
  margin: 0,
});

export const status = style({
  fontFamily: vars.font.mono,
  fontSize: vars.fontSize.caption,
  color: vars.color.muted,
});

export const messages = style({
  flex: 1,
  overflowY: "auto",
  paddingBlock: vars.space.sm,
});

export const empty = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  height: "100%",
  color: vars.color.mutedSoft,
  fontSize: vars.fontSize.bodySm,
});

export const errorLine = style({
  fontSize: vars.fontSize.bodySm,
  color: vars.color.error,
  paddingBlock: vars.space.xs,
});
