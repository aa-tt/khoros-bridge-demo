# DynamoDB

The stack creates on-demand tables with partition and sort keys and point-in-time recovery. Orders use `PK=ORDER#<id>` with a sort key for the order or its MSK projection. Salesforce snapshots use `PK=SF#Account#<recordId>` and timestamped snapshot sort keys; reports have a report index. Each table stores typed query fields and a compact JSON `payload` attribute for flexible source data. DynamoDB items have a 400 KB maximum, so raw exports and large reports belong in S3. Add GSIs only for defined query patterns.
