# Terraform

Infrastructure placeholder for AWS container deployment.

Recommended module split:

```txt
modules/network
modules/ecr
modules/ecs-service
modules/rds
modules/secrets
environments/dev
environments/prod
```

Keep application image tags and secret ARNs environment-specific.
