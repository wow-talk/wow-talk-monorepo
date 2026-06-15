import { style, styleVariants } from "@vanilla-extract/css";

const shellBase = style({
  display: "grid",
  height: "100dvh",
  width: "100%",
  transition: "grid-template-columns 220ms ease",

  "@media": {
    "(max-width: 768px)": {
      gridTemplateColumns: "1fr",
    },
  },
});

export const shell = styleVariants({
  open: [shellBase, { gridTemplateColumns: "1fr 360px" }],
  closed: [shellBase, { gridTemplateColumns: "1fr 0fr" }],
});
