# Salesforce sample data

`account-response.json` illustrates the Salesforce REST query response shape used by the scheduled sync. The Lambda example authenticates with a Connected App refresh token stored in Secrets Manager, maps Account summary fields to DynamoDB attributes, preserves the full source record as JSON, and writes the raw page set to S3 for traceable archival.
