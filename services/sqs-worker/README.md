# SQS worker

A Fargate worker long-polls its dedicated SQS queue, processes one message, publishes a `work.completed` event to MSK, then deletes the message. A message is deleted only after successful processing, so a failure can be retried and eventually routed to the queue's dead-letter queue.

Required environment: `AWS_REGION`, `WORK_QUEUE_URL`, `MSK_BOOTSTRAP_SERVERS`, and `MSK_ORDER_TOPIC`. The task role needs `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:GetQueueAttributes`, and MSK IAM write permissions to the event topic.
