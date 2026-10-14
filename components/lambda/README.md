# AWS Lambda

The Salesforce sync function is Python 3.12, scheduled by EventBridge. It exchanges a Connected App refresh token for an access token, aggregates Salesforce Account records, writes typed attributes plus the original JSON to DynamoDB, and archives the retrieved records in S3. It is intentionally separate from the long-running Fargate services. Package its source as a zip and place it in the private deployment-artifact bucket before creating the application stack. Its role is scoped to the configured secret, table, archive prefix, and CloudWatch logs.
