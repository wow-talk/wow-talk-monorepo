import type { z } from "zod";

import { env } from "@/lib/env";

/**
 * REST 호출의 단일 진입점.
 *
 * 컴포넌트/훅은 직접 `fetch`를 호출하지 말고 이 helper만 사용한다.
 * 응답 본문은 반드시 Zod 스키마로 parse → 계약 위반은 ApiError로 throw.
 */

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly body?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export class ApiSchemaError extends Error {
  constructor(
    public readonly path: string,
    public readonly issues: unknown,
  ) {
    super(`Response shape mismatch for ${path}`);
    this.name = "ApiSchemaError";
  }
}

export async function apiFetch<T>(
  path: string,
  schema: z.ZodType<T>,
  init?: RequestInit,
): Promise<T> {
  const url = `${env.NEXT_PUBLIC_API_BASE}${path}`;
  const res = await fetch(url, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
      ...init?.headers,
    },
  });

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new ApiError(res.status, `HTTP ${res.status} ${res.statusText}`, body);
  }

  const text = await res.text();
  const json: unknown = text ? JSON.parse(text) : null;

  const parsed = schema.safeParse(json);
  if (!parsed.success) {
    throw new ApiSchemaError(path, parsed.error.issues);
  }
  return parsed.data;
}
