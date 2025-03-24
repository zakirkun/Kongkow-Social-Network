# Swagger API Documentation for Kongkow Social Network

This guide explains how to use the Swagger UI to test and explore the Kongkow Social Network APIs.

## Accessing Swagger UI

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

Alternatively, you can access the raw OpenAPI specification at:

```
http://localhost:8080/api-docs
```

## Authentication in Swagger UI

Many of the API endpoints require authentication. To authenticate in Swagger UI:

1. Click on the "Authorize" button at the top right corner of the Swagger UI
2. In the "Value" field, enter your JWT token with the format: `Bearer your_token_here`
3. Click "Authorize" and then "Close"

You can obtain a JWT token by using the `POST /api/auth/login` endpoint.

## API Categories

The APIs are organized into the following categories:

1. **Authentication** - Login, signup, and logout endpoints
2. **User Management** - User profile operations
3. **Threads** - Create, read, update, and delete threads
4. **Comments** - Comment management and interaction
5. **Follows** - Follow/unfollow functionality
6. **Trending** - Discover trending content and search

## Testing the APIs

To test an API endpoint:

1. Click on the endpoint you want to test
2. Click the "Try it out" button
3. Fill in the required parameters
4. Click "Execute"
5. View the response

## Common Response Status Codes

- **200 OK**: The request was successful
- **201 Created**: A resource was successfully created
- **400 Bad Request**: The request was invalid or badly formed
- **401 Unauthorized**: Authentication is required or failed
- **403 Forbidden**: You don't have permission to access the resource
- **404 Not Found**: The requested resource doesn't exist
- **500 Internal Server Error**: Something went wrong on the server

## Response Models

The Swagger UI includes detailed models for all request and response objects. You can view these models by clicking on the "Schema" sections under each endpoint.

## Environment Configuration

If you're using different environments (development, staging, production), you may need to adjust the base URL in the Swagger UI according to your environment.

## Additional Resources

- [Swagger Documentation](https://swagger.io/docs/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Spring Boot Swagger Configuration](https://springdoc.org/)