# MSK worker

This independent consumer group reacts to `order.created` events on `bridge-events`, writes a processing record to DynamoDB, and commits its Kafka offset after persistence. Other services can consume the same topic independently without synchronously calling this worker.

Required environment: `AWS_REGION`, `MSK_BOOTSTRAP_SERVERS`, `MSK_ORDER_TOPIC`, and `ORDERS_TABLE`. The Fargate task role needs MSK IAM read/group permissions and `dynamodb:PutItem` on the order table. Tasks must have network connectivity to the brokers and use TLS/IAM authentication.
