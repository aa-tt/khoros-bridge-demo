# CloudFormation deployment

These templates describe the demo infrastructure; they do not deploy until you run the commands. Review costs and account policies first. The application stack creates paid resources including MSK Serverless, an internal load balancer, Fargate services, CloudFront, Lambda, and data storage.

## Prerequisites

- AWS CLI configured for a sandbox account and region; CloudFormation, IAM, ECS, MSK, API Gateway, Lambda, S3, and CloudFront permissions.
- An existing VPC with at least two private subnets in different AZs. Private subnets need outbound access to ECR and CloudWatch (NAT or suitable VPC endpoints); the internal ALB and Fargate tasks use private subnets.
- A Salesforce Connected App and a Secrets Manager secret with `client_id`, `client_secret`, `refresh_token`, and optional `login_url`.
- JDK 17+, Maven 3.9+, Docker, Node.js 20+, npm, and Python 3.12+ for building artifacts.

## Deployment order

1. Bootstrap the four private ECR repositories:

   ```powershell
   aws cloudformation deploy --template-file infra/cloudformation/ecr-bootstrap.yaml --stack-name khoros-bridge-ecr --region us-east-1
   ```

2. Deploy the MSK prerequisite stack. Use the same VPC and private subnets you plan to pass to the application stack:

   ```powershell
   aws cloudformation deploy --template-file infra/cloudformation/msk.yaml --stack-name khoros-bridge-msk --region us-east-1 --parameter-overrides VpcId=vpc-REPLACE PrivateSubnetIds=subnet-PRIVATEA,subnet-PRIVATEB
   aws cloudformation describe-stacks --stack-name khoros-bridge-msk --region us-east-1 --query "Stacks[0].Outputs" --output table
   aws kafka get-bootstrap-brokers --cluster-arn arn:aws:kafka:us-east-1:ACCOUNT:cluster/CLUSTER/UUID --query BootstrapBrokerStringSaslIam --output text
   ```

   Record the `ClusterArn` and `MskSecurityGroupId` outputs, and the broker string returned by `get-bootstrap-brokers`.
3. Build and push the four Java images from the repository root. Authenticate Docker to the ECR registry first. Use `services/Dockerfile` with build args `SERVICE=rest-service`, `sqs-worker`, `msk-worker`, and `reporting-service`. See `components/cicd/README.md`.
4. Package `lambda/salesforce-sync/handler.py` as `handler.zip` and upload it to a private S3 deployment-artifact bucket. Do not use the frontend bucket for Lambda code.
5. Deploy `application.yaml`, passing the VPC/subnets, MSK outputs, image URIs, Lambda artifact, and Salesforce secret parameters. It creates CloudFront/S3, API Gateway/VPC Link, ECS/Fargate, DynamoDB, SQS, EventBridge/Lambda, IAM, and CloudWatch resources.
6. Build `frontend/`, upload `frontend/dist/` to the `FrontendBucketName` output, and invalidate the CloudFront distribution. The CloudFront URL is the user-facing URL.

Example deployment (replace every placeholder; `--parameter-overrides` values are illustrative):

```powershell
aws cloudformation deploy --template-file infra/cloudformation/application.yaml --stack-name khoros-bridge-demo --region us-east-1 --capabilities CAPABILITY_IAM --parameter-overrides VpcId=vpc-REPLACE PrivateSubnetIds=subnet-PRIVATEA,subnet-PRIVATEB MskClusterArn=arn:aws:kafka:us-east-1:ACCOUNT:cluster/CLUSTER/UUID MskBootstrapServers=broker1:9098,broker2:9098 MskSecurityGroupId=sg-MSK RestImage=ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/khoros-bridge/rest-service:TAG SqsWorkerImage=ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/khoros-bridge/sqs-worker:TAG MskWorkerImage=ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/khoros-bridge/msk-worker:TAG ReportingImage=ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/khoros-bridge/reporting-service:TAG LambdaCodeBucket=PRIVATE-ARTIFACT-BUCKET LambdaCodeKey=lambda/handler.zip SalesforceSecretArn=arn:aws:secretsmanager:us-east-1:ACCOUNT:secret:sf-credentials
```

CloudFormation parameter list values are comma-separated. Use `aws cloudformation describe-stacks` to read outputs after deployment. Tear down the stack when finished to stop ongoing charges; retained/versioned S3 data and ECR images may need separate cleanup.

## Safety notes

This is a learning scaffold, not a production landing zone. It assumes an existing VPC and egress path, uses a demo HTTP API authorization setup, and does not create DNS/custom domains, WAF, KMS customer keys, Splunk/Datadog credentials, or full Salesforce pagination/rate-limit policy. Add authentication, TLS custom domain, alarms, data retention, backups, and organization-specific controls before production. Never commit secrets or use production credentials in a demo.
