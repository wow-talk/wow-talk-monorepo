# Terraform

Terraform for the AWS container deployment track.

The first implementation target is the dev API environment only. The frontend is
deployed separately on Vercel, so ECS/ECR should manage only the Spring Boot API
container.

## Dev Scope

- Region: `ap-northeast-2`
- Domain: `dev-api.wow-talk.com`
- Frontend origin: `https://dev.wow-talk.com`
- Container registry: one ECR repository for the API image
- Runtime: ECS Fargate API service, desired count `1`
- Networking: public subnets only, no NAT Gateway
- Entry point: public ALB with HTTP `80` redirecting to HTTPS `443`
- TLS/DNS: ACM certificate and Route53 alias for `dev-api.wow-talk.com`
- Logs: CloudWatch Logs with 7-day retention
- Config: SSM Parameter Store
- Data: DynamoDB tables, on-demand billing, PITR enabled
- CI/CD: GitHub Actions deploys API images from the `dev` branch using OIDC

The current backend source still contains JPA/Postgres wiring. DevOps work in
this repository is limited to `infra/`, so API task health is gated by the
backend switching the dev runtime to DynamoDB-compatible configuration.

## Recommended Layout

```txt
infra/terraform/
  bootstrap/
    main.tf
  modules/
    alb/
    dynamodb/
    ecr/
    ecs-api-service/
    github-oidc/
    network/
    ssm/
  environments/
    dev/
      backend.tf
      main.tf
      outputs.tf
      terraform.tfvars.example
      variables.tf
    prod/
      main.tf
```

## State Management

Use a remote Terraform backend from the beginning because development happens
from more than one local computer.

- S3 bucket for state
- DynamoDB table for state locking
- Bucket versioning enabled
- KMS encryption preferred when practical

Bootstrap state resources should be created separately before initializing
`environments/dev`.

## Image Deployment Boundary

Terraform creates the base AWS infrastructure and initial ECS task definition.
GitHub Actions owns application image rollout:

1. Build the API Docker image.
2. Push `dev-<short_sha>` and `dev-latest` tags to ECR.
3. Register a new ECS task definition revision with the immutable
   `dev-<short_sha>` image tag.
4. Update the dev ECS service.

Terraform should output the values required by the workflow instead of forcing
the workflow to hard-code resource names.
