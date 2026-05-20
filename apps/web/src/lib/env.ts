import { z } from "zod";

/**
 * 환경 변수 단일 진입점.
 *
 * 모든 NEXT_PUBLIC_* 키를 Zod로 parse한 뒤 `env` 객체 하나로 export한다.
 * 컴포넌트/모듈은 `process.env`를 직접 참조하지 말고 이 객체만 import.
 * parse 실패 시 모듈 로드 시점에 throw → 빌드 단계에서 즉시 발견.
 */

const envSchema = z.object({
  NEXT_PUBLIC_API_BASE: z
    .string()
    .url()
    .default("http://localhost:8080"),
  NEXT_PUBLIC_WS_BASE: z
    .string()
    .url()
    .default("ws://localhost:8080"),
});

const parsed = envSchema.safeParse({
  NEXT_PUBLIC_API_BASE: process.env.NEXT_PUBLIC_API_BASE,
  NEXT_PUBLIC_WS_BASE: process.env.NEXT_PUBLIC_WS_BASE,
});

if (!parsed.success) {
  throw new Error(
    `[env] 환경 변수 검증 실패: ${parsed.error.issues
      .map((i) => `${i.path.join(".")}: ${i.message}`)
      .join(", ")}`,
  );
}

export const env = parsed.data;
