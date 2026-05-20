"use client";

import { useMutation } from "@tanstack/react-query";

import { ensureChannel } from "@/lib/api/channels";
import type { RoomId } from "@/types/domain";

/**
 * 채팅 입장 직전 한 번 호출. 채널이 없으면 생성하고, 있으면 검증만.
 * mutation 형태인 이유: idempotent하지만 "사용자 행동에 따른 일회성 트리거"가 의도라 useQuery보다 useMutation이 의미상 적합.
 */
export function useEnsureChannel() {
  return useMutation({
    mutationFn: (roomId: RoomId) => ensureChannel(roomId),
  });
}
