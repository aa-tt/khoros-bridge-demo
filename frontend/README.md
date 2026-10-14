# React MFE

The Vite React app is hosted from a private S3 bucket and delivered by CloudFront. The distribution uses Origin Access Control (OAC) for the S3 origin and routes `/api/*` to API Gateway. Keep browser-side configuration non-secret; Salesforce and AWS credentials never belong in the frontend.

## Local development

```powershell
npm install
npm run dev
```

Set `VITE_API_BASE_URL` in a local `.env` only when calling a separately running API. Do not commit credentials. Production builds use relative `/api` paths so the same bundle works behind CloudFront.

## Build and publish

`npm run build` creates `dist/`. A deployment role uploads the contents to the CloudFormation-created frontend bucket and invalidates the CloudFront distribution. See `components/cicd/README.md` for GitHub Actions and CodeBuild options.
