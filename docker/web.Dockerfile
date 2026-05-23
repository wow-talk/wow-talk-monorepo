FROM node:22-alpine AS builder

WORKDIR /repo

RUN corepack enable

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/web/package.json ./apps/web/package.json

RUN pnpm install --frozen-lockfile

WORKDIR /repo
COPY apps/web ./apps/web

RUN pnpm --filter ./apps/web build

FROM node:22-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
RUN corepack enable

COPY --from=builder /repo/apps/web/package.json ./package.json
COPY --from=builder /repo/node_modules /node_modules
COPY --from=builder /repo/apps/web/node_modules ./node_modules
COPY --from=builder /repo/apps/web/.next ./.next
COPY --from=builder /repo/apps/web/src/app/favicon.ico ./src/app/favicon.ico

EXPOSE 3000

CMD ["pnpm", "start"]
