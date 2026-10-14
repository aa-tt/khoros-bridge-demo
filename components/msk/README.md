# Amazon MSK

MSK Serverless is the shared event bus. Services publish JSON envelopes to `bridge-events`; independent consumer groups for the MSK worker and reporting service react at their own pace. This is choreography, not a centralized workflow engine. The Java clients use TLS with SASL/IAM and must run in subnets with broker connectivity.

The cluster and its broker security group are created by `infra/cloudformation/msk.yaml` before the main application stack. MSK does not create this topic through CloudFormation. Before starting consumers, create `bridge-events` from a network-connected Kafka client with IAM permissions including `kafka-cluster:Connect`, `kafka-cluster:CreateTopic`, and `kafka-cluster:DescribeTopic`. Use the example properties file:

```sh
kafka-topics.sh --bootstrap-server "$MSK_BOOTSTRAP_SERVERS" --command-config infra/cloudformation/msk-client.properties.example --create --if-not-exists --topic bridge-events --partitions 3
```

For MSK Serverless, omit `--replication-factor` if the Kafka CLI rejects broker-managed replication settings. Keep topic configuration compatible with Serverless limits. Plan schema evolution, idempotent consumers, poison-event handling, retention, and partition keys before production use.
