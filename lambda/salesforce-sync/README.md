# Salesforce sync Lambda

EventBridge invokes this Python function on a configurable schedule. It exchanges a Salesforce refresh token for an access token, queries a small Account sample, writes typed summary fields plus the full record JSON into DynamoDB, and archives the raw response to S3. The example query is intentionally capped; paginate carefully and respect Salesforce API limits before adapting it to production.

The Secrets Manager JSON needs `client_id`, `client_secret`, `refresh_token`, and optionally `login_url` (defaults to Salesforce production login). Configure environment variables `SALESFORCE_SECRET_ARN`, `SALESFORCE_TABLE`, `ARCHIVE_BUCKET`, and optionally `SALESFORCE_API_VERSION`.

The Lambda role needs `secretsmanager:GetSecretValue` on the one secret, `dynamodb:PutItem` on the configured table, `s3:PutObject` on its archive prefix, and CloudWatch Logs permissions. The function is not placed in a VPC in the sample so it can reach Salesforce; if moved into private subnets, provide NAT or an approved egress proxy. Set a timeout, memory, reserved concurrency, retry policy, and EventBridge DLQ appropriate to the workload.
