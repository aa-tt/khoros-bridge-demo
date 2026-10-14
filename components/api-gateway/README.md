# API Gateway

The CloudFormation stack creates an HTTP API with an `$default` stage and VPC Link to an internal ALB. The ALB forwards `/api/*` to the REST Fargate service. CloudFront uses API Gateway as a separate origin and disables API response caching. The demo stack does not implement user authentication; add a JWT authorizer/Cognito integration and request throttling before production, and configure the React origin and CORS deliberately if calling the API outside CloudFront.
