import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "wow-talk",
  description: "WebSocket inspector chat demo",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
