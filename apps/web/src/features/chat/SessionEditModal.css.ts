import { style } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

export const backdrop = style({
  position: "fixed",
  inset: 0,
  backgroundColor: "rgba(20, 20, 19, 0.45)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  zIndex: 50,
});

export const dialog = style({
  backgroundColor: vars.color.canvas,
  borderRadius: vars.radius.lg,
  padding: vars.space.xl,
  width: "min(420px, 90vw)",
  display: "flex",
  flexDirection: "column",
  gap: vars.space.md,
  boxShadow: vars.shadow.soft,
});

export const title = style({
  fontFamily: vars.font.serif,
  fontSize: vars.fontSize.displaySm,
  fontWeight: vars.fontWeight.regular,
  letterSpacing: vars.letterSpacing.displaySm,
  margin: 0,
  color: vars.color.ink,
});

export const description = style({
  margin: 0,
  fontSize: vars.fontSize.bodySm,
  color: vars.color.body,
  lineHeight: vars.lineHeight.body,
});

export const input = style({
  height: "40px",
  paddingInline: vars.space.md,
  borderRadius: vars.radius.md,
  border: `1px solid ${vars.color.hairline}`,
  backgroundColor: vars.color.canvas,
  color: vars.color.ink,
  fontFamily: vars.font.mono,
  fontSize: vars.fontSize.bodyMd,

  selectors: {
    "&:focus": {
      outline: "none",
      borderColor: vars.color.primary,
      boxShadow: "0 0 0 3px rgba(204, 120, 92, 0.15)",
    },
  },
});

export const actions = style({
  display: "flex",
  gap: vars.space.sm,
  justifyContent: "flex-end",
});

const buttonBase = style({
  height: "40px",
  paddingInline: vars.space.lg,
  borderRadius: vars.radius.md,
  fontFamily: vars.font.sans,
  fontSize: vars.fontSize.button,
  fontWeight: vars.fontWeight.medium,
  lineHeight: vars.lineHeight.button,
  cursor: "pointer",
});

export const cancelButton = style([
  buttonBase,
  {
    backgroundColor: vars.color.canvas,
    color: vars.color.ink,
    border: `1px solid ${vars.color.hairline}`,
  },
]);

export const saveButton = style([
  buttonBase,
  {
    backgroundColor: vars.color.primary,
    color: vars.color.onPrimary,
    border: "none",

    selectors: {
      "&:hover:not(:disabled)": { backgroundColor: vars.color.primaryActive },
      "&:disabled": {
        backgroundColor: vars.color.primaryDisabled,
        color: vars.color.muted,
        cursor: "not-allowed",
      },
    },
  },
]);
