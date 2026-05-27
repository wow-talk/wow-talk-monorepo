import { style } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

export const header = style({
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  paddingInline: vars.space.md,
  paddingBlock: vars.space.sm,
  borderBottom: `1px solid ${vars.color.surfaceDarkElevated}`,
  backgroundColor: vars.color.surfaceDarkSoft,
});

export const title = style({
  display: "flex",
  alignItems: "center",
  gap: vars.space.xs,
  fontFamily: vars.font.mono,
  fontSize: vars.fontSize.captionUppercase,
  letterSpacing: vars.letterSpacing.uppercase,
  textTransform: "uppercase",
  color: vars.color.onDark,
  fontWeight: vars.fontWeight.medium,
});

export const dot = style({
  width: "8px",
  height: "8px",
  borderRadius: vars.radius.full,
  backgroundColor: vars.color.accentTeal,
});

export const count = style({
  color: vars.color.onDarkSoft,
  marginLeft: vars.space.xs,
});

export const close = style({
  width: "28px",
  height: "28px",
  borderRadius: vars.radius.sm,
  color: vars.color.onDarkSoft,
  fontSize: "20px",
  lineHeight: 1,
  cursor: "pointer",
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",

  selectors: {
    "&:hover": {
      backgroundColor: vars.color.surfaceDarkElevated,
      color: vars.color.onDark,
    },
  },
});
