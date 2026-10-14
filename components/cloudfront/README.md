# CloudFront and React hosting

CloudFront serves the Vite bundle from a private S3 origin protected with Origin Access Control. The `/api/*` behavior forwards API calls to API Gateway with caching disabled; static assets use caching. CloudFormation owns the distribution, OAC, bucket policy, and SPA fallback. Build output is uploaded to S3, then the distribution is invalidated during release. Keep secrets out of frontend bundles because browser code is public.
