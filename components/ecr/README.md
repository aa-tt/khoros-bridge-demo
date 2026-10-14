# Amazon ECR

`ecr-bootstrap.yaml` creates one private immutable image repository per Java service and enables scan-on-push. Build images from the repository root with `services/Dockerfile` and a `SERVICE` build argument. Tag each release with the Git commit SHA, authenticate the builder to ECR, and push the exact image URI used by the ECS task definition. Rotate base images and review scan findings regularly.
