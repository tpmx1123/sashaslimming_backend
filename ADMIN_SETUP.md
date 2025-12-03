# Admin Account Setup

## Default Admin Account

When the application starts for the first time, a default admin account is automatically created:

- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@slimming.com`

This is handled by the `DataInitializer` class which runs on application startup.

## Creating Additional Admins

You can create additional admin accounts using the registration endpoint:

### Using curl:
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newadmin","password":"securepassword123"}'
```

### Using Postman or similar:
- **URL**: `POST http://localhost:8081/api/auth/register`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "username": "newadmin",
  "password": "securepassword123"
}
```

## Login

Use the login endpoint to authenticate:

### Using curl:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@slimming.com",
  "role": "ADMIN"
}
```

## Important Notes

1. The default admin is only created if no admin with username "admin" exists
2. Passwords are encrypted using BCrypt before storage
3. After registration/login, use the JWT token in the Authorization header for protected endpoints:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

