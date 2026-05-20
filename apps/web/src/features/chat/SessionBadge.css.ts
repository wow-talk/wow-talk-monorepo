import { style, styleVariants } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

export const badge = style({
  display: "inline-flex",
  alignItems: "center",
  gap: vars.space.xs,
  paddingInline: vars.space.sm,
  paddingBlock: vars.space.xxs,
  borderRadius: vars.radius.pill,
  border: `1px solid ${vars.color.hairline}`,
  backgroundColor: vars.color.canvas,
  color: vars.color.ink,
  fontFamily: vars.font.mono,
  fontSize: vars.fontSize.caption,
  cursor: "pointer",

  selectors: {
    "&:hover": {
      borderColor: vars.color.primary,
    },
  },
});

const dotBase = style({
  width: "8px",
  height: "8px",
  borderRadius: vars.radius.full,
});

export const dot = styleVariants({
  open: [dotBase, { backgroundColor: vars.color.success }],
  connecting: [dotBase, { backgroundColor: vars.color.warning }],
  closed: [dotBase, { backgroundColor: vars.color.muted }],
  error: [dotBase, { backgroundColor: vars.color.error }],
  idle: [dotBase, { backgroundColor: vars.color.mutedSoft }],
});
