# Secure Twitter-like Application with Auth0

This repository contains a simplified Twitter-like application built in three stages:

1. A Spring Boot monolith secured with Auth0.
2. A React frontend that consumes the backend with access tokens issued by Auth0.
3. A serverless refactor into AWS Lambda microservices behind API Gateway.

The application lets authenticated users create short posts of up to 140 characters and read a single public stream/feed.

## Team Members

- David Sarria
- Vicente Garzon
- David Villadiego

## Final Architecture

```mermaid
flowchart LR
    U[User Browser] --> F[Frontend SPA\nReact + Auth0 React SDK]
    F -->|Login / Logout / Silent Token Refresh| A[Auth0]
    F -->|Bearer token| G[API Gateway]
    G --> U1[User Service Lambda\nGET /api/me]
    G --> P1[Posts Service Lambda\nPOST /api/posts]
    G --> S1[Stream Service Lambda\nGET /api/posts and /api/stream]
    P1 --> D[(DynamoDB posts table)]
    S1 --> D
    U1 --> A

    subgraph Monolith
      M[Spring Boot Monolith\nGET /api/posts, GET /api/stream, POST /api/posts, GET /api/me]
      M --> Mongo[(MongoDB)]
      M --> A
    end
```

The monolith lives in [Twitter_Application_Monolith](Twitter_Application_Monolith), while the serverless version lives in [Twitter_lambdas](Twitter_lambdas). The frontend is in [twitter_frontend_monolith](twitter_frontend_monolith).

## Project Structure

- [Twitter_Application_Monolith](Twitter_Application_Monolith): Spring Boot monolith with OpenAPI, Auth0 JWT validation, MongoDB persistence, and protected/public endpoints.
- [twitter_frontend_monolith](twitter_frontend_monolith): Vite + React SPA using the Auth0 React SDK.
- [Twitter_lambdas](Twitter_lambdas): Maven multi-module project with shared DTOs/utilities and three AWS Lambda services.
- [assets](assets): screenshots used as evidence for the AWS setup, tests, and final working application.

## Main Features

- Auth0 login and logout in the SPA.
- Silent token acquisition with scopes.
- Create posts with a strict 140-character limit.
- View a public global feed.
- Retrieve the current authenticated user through `/api/me`.
- Swagger/OpenAPI documentation in the monolith.
- AWS Lambda microservices for `user`, `posts`, and `stream`.

## API Overview

### Monolith Endpoints

- `GET /api/posts` and `GET /api/stream`: public stream of posts.
- `POST /api/posts`: create a post with a valid JWT access token.
- `GET /api/me`: return the authenticated user profile and granted scopes.

### Security Model

- The Spring Boot monolith is configured as an OAuth2 Resource Server.
- JWTs are validated using the Auth0 issuer URI and audience.
- Swagger UI is documented with a Bearer JWT security scheme.
- The SPA requests scopes such as `openid profile email read:profile write:posts`.
- The Lambda services also validate bearer tokens and fetch user information from the Auth0 `/userinfo` endpoint.

## Local Setup

### 1. Monolith

Requirements:

- Java 17
- Maven
- MongoDB

Run:

```bash
cd Twitter_Application_Monolith
mvn spring-boot:run
```

Useful URLs:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Environment variables:

```bash
MONGODB_URI=mongodb://localhost:27017/secure_twitter
AUTH0_ISSUER_URI=https://YOUR_DOMAIN.auth0.com/
AUTH0_AUDIENCE=YOUR_API_AUDIENCE
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:4173
```

### 2. Frontend

Requirements:

- Node.js 18+

Run:

```bash
cd twitter_frontend_monolith
npm install
npm run dev
```

Build for production:

```bash
npm run build
```

Environment variables:

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_AUTH0_DOMAIN=YOUR_DOMAIN.auth0.com
VITE_AUTH0_CLIENT_ID=YOUR_CLIENT_ID
VITE_AUTH0_AUDIENCE=YOUR_API_AUDIENCE
```

### 3. AWS Lambda Microservices

Requirements:

- Java 17
- Maven
- AWS account
- DynamoDB table named `posts`

Build all modules:

```bash
cd Twitter_lambdas
mvn clean package
```

Artifacts are generated in each module target folder:

- `Twitter_lambdas/user-service-lambda/target/user-service-lambda.jar`
- `Twitter_lambdas/posts-service-lambda/target/posts-service-lambda.jar`
- `Twitter_lambdas/stream-service-lambda/target/stream-service-lambda.jar`

Expected Lambda environment variables:

```bash
AWS_REGION=us-east-1
DYNAMODB_TABLE_NAME=posts
AUTH0_ISSUER_URI=https://YOUR_DOMAIN.auth0.com/
```

## AWS Deployment Summary

### Frontend

- The SPA is built with Vite and uploaded to Amazon S3 as static content.
- CloudFront is used to distribute the frontend publicly.
- The S3 bucket must allow the required static website hosting configuration used in the screenshots.

### Backend

- The monolith was first validated with MongoDB and Auth0.
- The serverless version was split into three Lambda functions:
  - `user-service` for `/api/me`
  - `posts-service` for `POST /api/posts`
  - `stream-service` for `GET /api/posts` and `GET /api/stream`
- API Gateway routes were mapped to the corresponding Lambda integrations.
- CORS was configured to allow the deployed frontend origin.

### Auth0

- A SPA client was created for the frontend.
- A dedicated API was created with its own audience.
- The frontend uses the official Auth0 React SDK.
- The backend validates JWT access tokens and enforces the audience.

## Tests Performed

The following validations were performed and/or demonstrated during deployment:

- Frontend production build with `npm run build`.
- Lambda packaging with Maven `clean package`.
- Lambda test invocations for user, posts, and stream services.
- Successful creation of a post with a valid JWT.
- Unauthorized response when invoking the protected posts endpoint with an invalid token.
- Public feed retrieval from the stream service.
- Manual end-to-end verification in the deployed SPA: login, logout, create post, refresh feed, and load `/api/me`.

## Screenshots

### Frontend and Final App

![Frontend build](assets/3-BuildDelFront.png)

![Frontend deployed on S3/CloudFront](assets/11-FrontendDesplegado.png)

![Final working Twitter-like app](assets/28-TwitterFuncionando.png)

![Long post validation](assets/29-MuchosCaracteresPrueba.png)

### Swagger

![Swagger UI screenshot](assets/swagger.png)

### Storage and Delivery

![S3 bucket created](assets/1-BucketCreation.png)

![S3 public access blocked](assets/2-BlockPublicAccessInBucket.png)

![Compiled frontend uploaded to S3](assets/4-UploadDelCompiladoAlS3.png)

![CloudFront created](assets/5-CreamosElCloudFront.png)

![S3 selected as CloudFront origin](assets/6-SeleccionamosS3.png)

![Final CloudFront distribution](assets/7-CreacionFinalDelCloudFront.png)

![HTTP vs HTTPS deployment issue](assets/10-ErrorHTTP-HTTPS.png)

During deployment we found an infrastructure limitation: the frontend could not be reliably left on plain S3 website hosting because Auth0 SPA flows require a secure origin (HTTPS) in production. The S3 static website endpoint is HTTP-only, so Auth0 rejects the app in that scenario. The correct solution is to place CloudFront in front of S3 to provide HTTPS, but that step was initially blocked by IAM permissions in AWS (insufficient permissions to create/configure the distribution). In short, the issue was not the frontend code, but missing cloud permissions plus the HTTPS requirement enforced by Auth0. Therefore, the frontend was finally deployed on Render.

### Serverless Back-End

![Lambda functions](assets/21-LambdaFunctions.png)

![API Gateway configuration](assets/22-APIGATEWAYCONFIG.png)

![User Lambda integration](assets/23-user-serviceLambdaIntegracionEnAPIGateway.png)

![Posts Lambda integration](assets/24-posts-serviceLambdaIntegracionEnAPIGateway.png)

![Stream Lambda integration](assets/25-stream-serviceLambdaIntegracionEnAPIGateway.png)

![Routes mapped to Lambda integrations](assets/26-CreamosRutasYAsignamosIntegracionesLambdaACadaRuta.png)

![API Gateway deployed](assets/27-DeployAPIGateway.png)

### Lambda Test Evidence

![User Lambda created](assets/12-user-serviceLambdaCreacion.png)

![User Lambda test success](assets/13-user-serviceLambdaTestingWorks.png)

![Posts Lambda created](assets/14-post-serviceLambdaCreacion.png)

![Posts Lambda test success](assets/15-post-serviceLambdaTesting201Works.png)

![Posts Lambda JSON test](assets/16-post-serviceLambdaTesting201WorksJSON.png)

![Posts Lambda unauthorized test](assets/17-post-serviceLambdaTesting401NoworkBadToken.png)

![Stream Lambda created](assets/18-stream-serviceLambdaCreacion.png)

![Stream Lambda test with posts](assets/19-stream-serviceLambda200TestWithPosts.png)

![Stream Lambda test JSON](assets/20-stream-serviceLambdaTestJSON.png)

### DynamoDB

![DynamoDB table creation](assets/11-DynamoDBCreacion.png)

## LinkS

- Frontend demo: [https://twitter-application-sd.onrender.com](https://twitter-application-sd.onrender.com)
- Video: https://pruebacorreoescuelaingeduco-my.sharepoint.com/:v:/g/personal/david_villadiego-m_mail_escuelaing_edu_co/IQAG7tz9peiAQ4Wjhkvzl1BhAcgLZ3a2PDt7oA4IXEIgVGY?nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJPbmVEcml2ZUZvckJ1c2luZXNzIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXciLCJyZWZlcnJhbFZpZXciOiJNeUZpbGVzTGlua0NvcHkifX0&e=ZqMBBD