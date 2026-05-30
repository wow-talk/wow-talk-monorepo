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
pnpm local:up
pnpm dev:api
pnpm dev:web
```

The frontend reads:

```txt
NEXT_PUBLIC_API_BASE=http://localhost:8080
NEXT_PUBLIC_WS_BASE=ws://localhost:8080
```

The backend defaults to the `local` Spring profile. Local MVP persistence still uses Postgres, and DynamoDB Local is available for the upcoming room event storage adapter.

## Build

```bash
pnpm build:web
pnpm build:api
```

## Documentation

```txt
docs/          Product, backend, API contract, infrastructure direction
apps/web/docs  Frontend implementation notes and learning records
```

Frontend/backend shared contracts live in:

- [docs/team-workflow.md](docs/team-workflow.md)
- [docs/frontend-backend-contract.md](docs/frontend-backend-contract.md)
- [docs/websocket-api.md](docs/websocket-api.md)
- [docs/realtime-protocol-v1.md](docs/realtime-protocol-v1.md)
- [docs/realtime-scaleout.md](docs/realtime-scaleout.md)
- [docs/team-workflow.md](docs/team-workflow.md)

## AWS deployment direction

The target shape is separate container images for `web` and `api`, pushed to ECR and deployed as separate ECS/Fargate services behind an ALB.

The backend should assume multiple API tasks from the beginning.

```txt
ALB
  /      -> web service
  /api   -> api service
  /ws    -> api service

api service
  desired count >= 3
```

Production storage direction:

```txt
DynamoDB
  user / room / member state
  room event stream
  chat messages
  game events
```

Realtime scale-out direction:

```txt
WebSocket connections are local to each API task.
Room events must be published through a realtime broker so every API task can broadcast to its local sockets.
```

See [docs/realtime-scaleout.md](docs/realtime-scaleout.md) for the scale-out design.
