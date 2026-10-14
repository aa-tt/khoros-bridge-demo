# SQS

The REST service places work messages on a standard SQS queue. The Fargate SQS worker long-polls, processes, emits a completion event to MSK, and deletes only successfully processed messages. The queue uses a visibility timeout and redrive policy to a dead-letter queue. Monitor queue age and DLQ depth in CloudWatch; redrive only after fixing the cause. SQS and MSK solve different problems here: queue work retry/visibility versus durable event distribution to multiple consumers.
