# Reporting service

A separate MSK consumer group creates an event-level JSON report artifact for each event, stores it under a dated S3 prefix, and records its location in DynamoDB. This is a simple demonstration; production reporting would batch events, define retention and access controls, and could catalog S3 data for Athena.

Required environment: `AWS_REGION`, `MSK_BOOTSTRAP_SERVERS`, `MSK_ORDER_TOPIC`, `MSK_GROUP_ID`, `REPORTS_BUCKET`, and `REPORTS_TABLE`. The task role needs MSK IAM read/group permissions, `s3:PutObject` to the report prefix, and `dynamodb:PutItem` on the report index table.
