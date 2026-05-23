# wow-talk

Wow Talk monorepo.

## Structure

```txt
apps/web        Next.js frontend
apps/api        Spring Boot API application
backend/core    Backend domain and persistence module
backend/transport
backend/websocket
backend/rawtcp
infra/terraform Terraform infrastructure
docker          Production Dockerfiles
```

## Local development

```bash
pnpm install
pnpm db:up
pnpm dev:api
pnpm dev:web
```

The frontend reads:

```txt
NEXT_PUBLIC_API_BASE=http://localhost:8080
NEXT_PUBLIC_WS_BASE=ws://localhost:8080
```

The backend defaults to the `local` Spring profile and uses the Postgres service in `compose.yaml`.

## Build

```bash
pnpm build:web
pnpm build:api
```

## AWS deployment direction

The target shape is separate container images for `web` and `api`, pushed to ECR and deployed as separate ECS/Fargate services behind an ALB. PostgreSQL should be managed with RDS, with credentials injected through Secrets Manager or SSM Parameter Store.
