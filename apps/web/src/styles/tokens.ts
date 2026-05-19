/**
 * DESIGN.md의 디자인 토큰 raw values.
 *
 * 이 파일이 SSOT. `theme.css.ts`가 이 객체를 `createGlobalTheme(':root', tokens)`로
 * 감싸서 CSS 변수로 노출한다. 컴포넌트 스타일은 항상 `vars.*`만 참조한다.
 *
 * 매핑표는 docs/50-styling/01-design-tokens-mapping.md 참조.
 */
export const tokens = {
  color: {
    // brand & accent
    primary: "#cc785c",
    primaryActive: "#a9583e",
    primaryDisabled: "#e6dfd8",
    accentTeal: "#5db8a6",
    accentAmber: "#e8a55a",

    // surface (cream side)
    canvas: "#faf9f5",
    surfaceSoft: "#f5f0e8",
    surfaceCard: "#efe9de",
    surfaceCreamStrong: "#e8e0d2",

    // surface (dark side)
    surfaceDark: "#181715",
    surfaceDarkElevated: "#252320",
    surfaceDarkSoft: "#1f1e1b",

    // hairline
    hairline: "#e6dfd8",
    hairlineSoft: "#ebe6df",

    // text on cream
    ink: "#141413",
    bodyStrong: "#252523",
    body: "#3d3d3a",
    muted: "#6c6a64",
    mutedSoft: "#8e8b82",

    // text on coral / dark
    onPrimary: "#ffffff",
    onDark: "#faf9f5",
    onDarkSoft: "#a09d96",

    // semantic
    success: "#5db872",
    warning: "#d4a017",
    error: "#c64545",
  },

  font: {
    // next/font가 layout에서 --font-serif/sans/mono CSS 변수를 주입.
    // 여기서는 fallback chain 끝까지 명시.
    serif:
      'var(--font-serif), "Cormorant Garamond", Garamond, "Times New Roman", serif',
    sans: 'var(--font-sans), Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    mono: 'var(--font-mono), "JetBrains Mono", "Courier New", monospace',
  },

  fontSize: {
    displayXl: "64px",
    displayLg: "48px",
    displayMd: "36px",
    displaySm: "28px",
    titleLg: "22px",
    titleMd: "18px",
    titleSm: "16px",
    bodyMd: "16px",
    bodySm: "14px",
    caption: "13px",
    captionUppercase: "12px",
    code: "14px",
    button: "14px",
    navLink: "14px",
  },

  fontWeight: {
    regular: "400",
    medium: "500",
  },

  lineHeight: {
    displayXl: "1.05",
    displayLg: "1.1",
    displayMd: "1.15",
    displaySm: "1.2",
    title: "1.3",
    titleTight: "1.4",
    body: "1.55",
    code: "1.6",
    button: "1.0",
  },

  letterSpacing: {
    displayXl: "-1.5px",
    displayLg: "-1px",
    displayMd: "-0.5px",
    displaySm: "-0.3px",
    normal: "0",
    uppercase: "1.5px",
  },

  space: {
    xxs: "4px",
    xs: "8px",
    sm: "12px",
    md: "16px",
    lg: "24px",
    xl: "32px",
    xxl: "48px",
    section: "96px",
  },

  radius: {
    xs: "4px",
    sm: "6px",
    md: "8px",
    lg: "12px",
    xl: "16px",
    pill: "9999px",
    full: "9999px",
  },

  shadow: {
    soft: "0 1px 3px rgba(20, 20, 19, 0.08)",
  },
} as const;
