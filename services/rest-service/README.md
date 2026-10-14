# REST service

Spring Boot API behind API Gateway and an internal load balancer. `POST /api/orders` stores an order in DynamoDB, enqueues work in SQS, and publishes `order.created` to MSK. The demo does not make a request idempotent: a retry can create another order, and a failure between writes can leave partial dispatch. Production clients should send an idempotency key, and the service should enforce it with a conditional write or transactional outbox pattern.

## Environment

- `AWS_REGION`: AWS region used by the SDK.
- `ORDERS_TABLE`: DynamoDB table name.
- `WORK_QUEUE_URL`: SQS URL for follow-up work.
- `MSK_BOOTSTRAP_SERVERS`: IAM-authenticated broker list.
- `MSK_ORDER_TOPIC`: event topic (default `bridge-events`).

The ECS task role needs `dynamodb:PutItem`, `sqs:SendMessage`, and MSK IAM data-plane permissions for the named table, queue, and topic. No static AWS keys are used.
