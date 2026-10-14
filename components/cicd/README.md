# CI/CD: CodeBuild or GitHub Actions

Both pipelines can use the same sequence: checkout; run Maven tests/package; build and push four service images to ECR; zip the Lambda handler and upload to a private artifacts bucket; deploy/update CloudFormation; build the React bundle; sync `frontend/dist/` to the stack output bucket; invalidate CloudFront. Provisioning and application deployment can be separate workflows so ordinary code releases do not change network infrastructure.

## GitHub Actions

Use GitHub's AWS OIDC federation and a narrowly scoped IAM deploy role; do not save long-lived AWS keys as repository secrets. Configure account ID, region, role ARN, artifact bucket, environment-specific network parameters, and Salesforce secret ARN as protected variables/secrets. Run the workflow on a reviewed main-branch push or manual dispatch. Tag images with the commit SHA and deploy those exact immutable tags.

## AWS CodeBuild

Use a CodeBuild project with a source connection to GitHub, an IAM service role with ECR upload, artifact S3 upload, and scoped CloudFormation permissions, plus a `buildspec.yml`. CodePipeline can trigger CodeBuild and add approval gates. Use Parameter Store/Secrets Manager references for sensitive values.

The repository intentionally does not enable a deployment workflow automatically: users must choose an AWS account, VPC, subnets, region, approval policy, and build platform first.
