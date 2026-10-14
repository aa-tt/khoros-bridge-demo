# S3 reporting and archive

A private S3 bucket stores Salesforce snapshots and generated event reports under separate prefixes. The React bundle uses its own private bucket and CloudFront Origin Access Control; browsers do not receive S3 public access. Server-side encryption, versioning, block-public-access settings, and lifecycle transitions are configured in CloudFormation. Define retention/deletion rules to match data policy; lifecycle transitions alone do not delete data. Consider Athena and Glue cataloging for larger report datasets.
