# ECS and Fargate

Four independent ECS services run on Fargate: REST, SQS worker, MSK worker, and reporting. Each task definition has its own application task role, while the ECS task execution role handles ECR pulls and CloudWatch log delivery. The API service is registered with the internal ALB; worker services have no inbound listener. Keep tasks in private subnets and provide a deliberate egress route to ECR, CloudWatch, Salesforce (for Lambda if VPC-attached), and MSK. No application EC2 instances are provisioned.
