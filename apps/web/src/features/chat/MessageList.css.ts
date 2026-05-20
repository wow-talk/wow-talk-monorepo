import { style } from "@vanilla-extract/css";

import { vars } from "@/styles/theme.css";

export const list = style({
  display: "flex",
  flexDirection: "column",
  gap: vars.space.sm,
  listStyle: "none",
  margin: 0,
  padding: 0,
});

export const anchor = style({
  height: "1px",
});
