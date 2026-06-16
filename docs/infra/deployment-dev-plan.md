# Dev Deployment Plan

## Purpose

This document fixes the first dev infrastructure plan for Wow Talk.

The immediate goal is to prepare a low-cost AWS dev environment for the backend
API while keeping the frontend on Vercel. This is a DevOps-owned plan and should
not require backend source changes inside this branch.

## Decisions

### Scope

- Deploy only the API container through ECS/ECR.
- Deploy the frontend through Vercel.
- Do not deploy the Next.js frontend to ECS in the first dev phase.
- Do not create RDS or any PostgreSQL runtime infrastructure.
- Keep Terraform apply separate from application deployment.

### Domains

- API domain: `dev-api.wow-talk.com`
- Frontend domain: `dev.wow-talk.com`
- DNS provider: Route53
- API traffic must use HTTPS.
- ALB HTTP listener on port `80` redirects to HTTPS port `443`.

### AWS Region

- Region is fixed to `ap-northeast-2`.
- ECS, ECR, ALB, ACM, DynamoDB, SSM, and CloudWatch resources should stay in the
  same region.

### Terraform State

- Use S3 remote state.
- Use DynamoDB state locking.
- Bootstrap the state bucket and lock table before the dev environment.
- The same AWS account and region will be used from two local computers, so
  local Terraform state is not acceptable for the shared dev environment.

### Networking

- Use public subnets only for the first dev environment.
- Do not create a NAT Gateway.
- ECS Fargate tasks may use public IPs to avoid NAT cost.
- Security groups must still restrict inbound traffic:
  - ALB allows public `80` and `443`.
  - API task allows inbound traffic only from the ALB security group on
    container port `8080`.

This is a deliberate cost tradeoff for a portfolio/dev environment. Production
can move API tasks to private subnets later.

### API Runtime

- ECS launch type: Fargate
- API desired count: `1`
- CPU: `256`
- Memory: `512`
- Container port: `8080`
- ALB idle timeout: `300` seconds for WebSocket tolerance
- Health check path: `/v3/api-docs` until the backend provides a dedicated
  health endpoint
- Runtime profile target: separate dev profile, for example
  `SPRING_PROFILES_ACTIVE=dev`

The current backend code still contains Postgres/JPA wiring. The ECS service
infrastructure can be prepared, but stable task health depends on the backend
runtime no longer requiring a PostgreSQL DataSource.

### Configuration

- Use SSM Parameter Store for dev runtime configuration.
- Do not use Secrets Manager in the first dev phase unless real rotating secrets
  are introduced.
- Set backend CORS to the fixed Vercel frontend origin:
  `WOWTALK_CORS_ALLOWED_ORIGINS=https://dev.wow-talk.com`

### Logging

- Use CloudWatch Logs only.
- Log group retention: 7 days.
- Do not add FireLens, S3 log archival, or external log routing in the first dev
  phase.

### DynamoDB

Use DynamoDB as the planned dev data layer.

Billing and recovery:

- Billing mode: `PAY_PER_REQUEST`
- Point-in-time recovery: enabled
- Deletion protection: not required for dev unless the team explicitly changes
  the lifecycle policy

Initial table set:

| Table | Partition key | Sort key | Streams |
| --- | --- | --- | --- |
| `users` | `userId` | none | off |
| `rooms` | `roomId` | none | off |
| `room_members` | `roomId` | `userId` | off |
| `chat_messages` | `roomId` | `sentAtMessageId` | on |
| `room_events` | `roomId` | `createdAtEventId` | on |
| `game_events` | `gameId` | `createdAtEventId` | on |

Streams should use `NEW_IMAGE` for event-oriented tables. No stream consumer is
part of the first infra phase.

### ECR

- Create one API ECR repository, for example `wow-talk-dev-api`.
- Enable image scan on push.
- Keep the repository mutable for convenience, but deploy immutable image tags.
- Push both:
  - `dev-<short_sha>`
  - `dev-latest`
- ECS should deploy `dev-<short_sha>`.

### GitHub Actions

- Trigger: push to `dev` branch.
- Auth: GitHub OIDC AssumeRole.
- Trust policy should be restricted to the repository and
  `refs/heads/dev`.
- Terraform should create the OIDC provider and deployment role.
- GitHub Actions should not run Terraform apply in the first phase.

Workflow responsibility:

1. Build the API image.
2. Push the image to ECR.
3. Register a new ECS task definition revision with the new image tag.
4. Update the ECS service.

The deployment role should have application deployment permissions only, not
broad infrastructure mutation permissions.

## Terraform Module Plan

Recommended modules:

- `network`: VPC, public subnets, route tables, internet gateway, security groups
- `ecr`: API repository
- `alb`: ALB, listeners, target group, ACM certificate, Route53 record
- `ecs-api-service`: cluster, task definition, service, IAM task roles
- `dynamodb`: dev application tables and PITR/Streams settings
- `ssm`: application configuration parameters
- `github-oidc`: OIDC provider and dev deploy role

Recommended environments:

- `bootstrap`: S3 state bucket and DynamoDB lock table
- `environments/dev`: full dev API environment
- `environments/prod`: placeholder only for now

## Required Outputs

The dev Terraform environment should output values needed by GitHub Actions and
Vercel configuration:

- `aws_region`
- `ecr_repository_url`
- `ecs_cluster_name`
- `ecs_service_name`
- `ecs_task_definition_family`
- `api_container_name`
- `github_actions_role_arn`
- `api_base_url`
- `ws_base_url`

## Non-Goals

- No RDS.
- No NAT Gateway.
- No ECS frontend service.
- No Terraform apply from GitHub Actions.
- No production environment implementation.
- No backend source changes in this DevOps branch.
- No realtime broker implementation in the first infra phase.
- No DynamoDB Streams consumer in the first infra phase.

## Risks And Gates

- Backend runtime gate: the current backend may fail to start without Postgres.
  This must be resolved by backend work before ECS API health can become green.
- Health check gate: `/v3/api-docs` is acceptable for dev bootstrap, but a
  dedicated health endpoint is preferable.
- Public task tradeoff: Fargate tasks use public IPs to avoid NAT cost. Security
  groups must strictly limit inbound access to ALB-originated traffic.
- DynamoDB schema risk: table and key choices should be revisited when backend
  access patterns are implemented.
- Streams risk: Streams are enabled for event-oriented tables, but no consumer
  exists yet.

## First Implementation Order

1. Create Terraform bootstrap state resources.
2. Configure `environments/dev` remote backend.
3. Add the network module with public subnets and no NAT Gateway.
4. Add API ECR.
5. Add DynamoDB dev tables.
6. Add SSM parameters.
7. Add ACM, Route53, and ALB.
8. Add ECS cluster, task definition, and API service.
9. Add GitHub OIDC deploy role.
10. Add GitHub Actions workflow for API image deployment.
11. Document Vercel environment values:
    - `NEXT_PUBLIC_API_BASE=https://dev-api.wow-talk.com`
    - `NEXT_PUBLIC_WS_BASE=wss://dev-api.wow-talk.com`
