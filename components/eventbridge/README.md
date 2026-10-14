# Amazon EventBridge

An EventBridge scheduled rule periodically invokes only the Salesforce sync Lambda through a scoped Lambda resource permission. The sample uses `rate(1 hour)`; change `SyncScheduleExpression` for the environment and expected Salesforce API quota. For production, add a failure destination or DLQ, retry policy, duplicate-safe writes, and a CloudWatch alarm. EventBridge schedules the sync; it does not orchestrate the MSK event choreography.
