# EventBridge and Lambda

An EventBridge schedule invokes the Python Salesforce sync Lambda. The function obtains a Connected App refresh token from Secrets Manager, queries Salesforce, writes typed fields and JSON payloads to DynamoDB, and archives the source response in S3. The sample has bounded query size and a basic timeout; set retries, a DLQ, concurrency, API limits, alerting, and secret rotation for a real integration. EventBridge has invoke permission only for this function and schedule.
