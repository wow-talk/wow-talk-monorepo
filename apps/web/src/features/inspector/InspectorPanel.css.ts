import { style, styleVariants } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

const panelBase = style({
  display: "flex",
  flexDirection: "column",
  backgroundColor: vars.color.surfaceDark,
  color: vars.color.onDark,
  fontFamily: vars.font.mono,
  fontSize: vars.fontSize.code,
  overflow: "hidden",
  borderLeft: `1px solid ${vars.color.surfaceDarkElevated}`,
  transition: "transform 220ms ease, opacity 220ms ease",

  "@media": {
    "(max-width: 768px)": {
      position: "fixed",
      inset: 0,
      zIndex: 40,
      borderLeft: "none",
    },
  },
});

export const panel = styleVariants({
  open: [
    panelBase,
    {
      transform: "translateX(0)",
      opacity: 1,
    },
  ],
  closed: [
    panelBase,
    {
      transform: "translateX(100%)",
      opacity: 0,
      pointerEvents: "none",
    },
  ],
});

export const body = style({
  flex: 1,
  overflowY: "auto",
  padding: vars.space.md,
  display: "flex",
  flexDirection: "column",
  gap: vars.space.xxs,
});

export const anchor = style({
  height: "1px",
});

export const empty = style({
  color: vars.color.onDarkSoft,
  fontStyle: "italic",
  textAlign: "center",
  marginTop: vars.space.lg,
});
