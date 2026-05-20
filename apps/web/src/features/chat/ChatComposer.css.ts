import { style } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

export const form = style({
  display: "flex",
  gap: vars.space.sm,
  paddingTop: vars.space.sm,
  borderTop: `1px solid ${vars.color.hairline}`,
});

export const input = style({
  flex: 1,
  height: "40px",
  paddingInline: vars.space.md,
  borderRadius: vars.radius.md,
  border: `1px solid ${vars.color.hairline}`,
  backgroundColor: vars.color.canvas,
  color: vars.color.ink,
  fontFamily: vars.font.sans,
  fontSize: vars.fontSize.bodyMd,

  selectors: {
    "&:focus": {
      outline: "none",
      borderColor: vars.color.primary,
      boxShadow: `0 0 0 3px rgba(204, 120, 92, 0.15)`,
    },
    "&:disabled": {
      backgroundColor: vars.color.surfaceSoft,
      color: vars.color.muted,
      cursor: "not-allowed",
    },
  },
});

export const submit = style({
  height: "40px",
  paddingInline: vars.space.lg,
  borderRadius: vars.radius.md,
  backgroundColor: vars.color.primary,
  color: vars.color.onPrimary,
  fontFamily: vars.font.sans,
  fontSize: vars.fontSize.button,
  fontWeight: vars.fontWeight.medium,
  lineHeight: vars.lineHeight.button,
  border: "none",
  cursor: "pointer",

  selectors: {
    "&:hover:not(:disabled)": {
      backgroundColor: vars.color.primaryActive,
    },
    "&:disabled": {
      backgroundColor: vars.color.primaryDisabled,
      color: vars.color.muted,
      cursor: "not-allowed",
    },
  },
});
