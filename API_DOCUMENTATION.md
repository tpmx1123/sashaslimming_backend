# LumiereLuxe Saloon API Documentation

## Base URL
- **Development**: `http://localhost:8081`
- **Production**: `https://lumiereluxe.in/api` (or your production domain)

## Authentication
Most endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## FEATURE I - Admin Authentication

### Models

#### Admin Model
```json
{
  "id": {number, auto-generated},
  "username": {string, mandatory, unique},
  "password": {string, mandatory, encrypted with bcrypt},
  "email": {string, mandatory},
  "role": {string, default: "ADMIN"},
  "createdAt": {timestamp, auto-generated},
  "resetToken": {string, nullable},
  "resetTokenExpiry": {timestamp, nullable}
}
```

---

### Admin APIs

#### POST /api/auth/register
Create an admin user document from request body.

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return the admin document with JWT token.
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.
```json
{
  "error": "Username already exists"
}
```

**Status Codes:**
- `200` - Success
- `400` - Bad Request (missing fields, username already exists)

---

#### POST /api/auth/login
Allow an admin to login with their username and password.

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response Format:**

**On Success** - Return HTTP status 200 and JWT token in response body.
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.
```json
{
  "error": "Invalid username or password"
}
```

**Status Codes:**
- `200` - Success
- `400` - Bad Request (invalid credentials, missing fields)

---

#### POST /api/auth/forgot-password
Request a password reset link. Sends reset email to configured admin email.

**Request Body:**
```json
{
  "username": "admin"
}
```

**Response Format:**

**On Success** - Return HTTP status 200.
```json
{
  "success": true,
  "message": "Password reset link has been sent to lumiereluxe0030@gmail.com"
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.
```json
{
  "error": "Username is required"
}
```

**Status Codes:**
- `200` - Success (always returns success for security reasons)
- `400` - Bad Request (missing username)

---

#### POST /api/auth/reset-password
Reset password using the reset token received via email.

**Request Body:**
```json
{
  "token": "uuid-reset-token",
  "newPassword": "newpassword123"
}
```

**Response Format:**

**On Success** - Return HTTP status 200.
```json
{
  "success": true,
  "message": "Password reset successfully"
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.
```json
{
  "error": "Invalid or expired reset token"
}
```

**Status Codes:**
- `200` - Success
- `400` - Bad Request (invalid/expired token, weak password)

---

#### POST /api/auth/change-password (Authentication required)
Allow an authenticated admin to change their password.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "currentPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

**Response Format:**

**On Success** - Return HTTP status 200.
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.
```json
{
  "error": "Current password is incorrect"
}
```

**Status Codes:**
- `200` - Success
- `400` - Bad Request (incorrect current password, weak new password)
- `401` - Unauthorized (missing/invalid token)

---

## FEATURE II - Blog Management

### Models

#### Blog Model
```json
{
  "id": {number, auto-generated},
  "title": {string, mandatory},
  "excerpt": {string, mandatory, text},
  "image": {string, mandatory}, // Cloudinary/S3 link
  "category": {string, mandatory},
  "readTime": {string, default: "5 min read"},
  "content": {string, mandatory, text},
  "createdAt": {timestamp, auto-generated},
  "updatedAt": {timestamp, auto-updated},
  "isFeatured": {boolean, default: false},
  "status": {string, enum: ["DRAFT", "PUBLISHED"], default: "DRAFT"}
}
```

---

### Blog APIs (No authentication required for public endpoints)

#### GET /api/blogs/public/all
Returns all published blogs in the collection.

**Response Format:**

**On Success** - Return HTTP status 200. Also return the blog documents.
```json
[
  {
    "id": 1,
    "title": "Hair Care Tips",
    "excerpt": "Essential tips for maintaining healthy hair...",
    "image": "https://res.cloudinary.com/...",
    "category": "Hair Care",
    "readTime": "5 min read",
    "content": "Full blog content...",
    "createdAt": "2024-01-10T06:25:46.051Z",
    "updatedAt": "2024-01-10T06:25:46.051Z",
    "isFeatured": true,
    "status": "PUBLISHED"
  }
]
```

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success

---

#### GET /api/blogs/public/{id}
Returns blog details by blog id or slug.

**Path Parameters:**
- `id` - Blog ID (number) or slug (string)

**Response Format:**

**On Success** - Return HTTP status 200. Also return the blog document.
```json
{
  "id": 1,
  "title": "Hair Care Tips",
  "excerpt": "Essential tips for maintaining healthy hair...",
  "image": "https://res.cloudinary.com/...",
  "category": "Hair Care",
  "readTime": "5 min read",
  "content": "Full blog content...",
  "createdAt": "2024-01-10T06:25:46.051Z",
  "updatedAt": "2024-01-10T06:25:46.051Z",
  "isFeatured": true,
  "status": "PUBLISHED"
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `404` - Blog not found

---

#### GET /api/blogs/public/featured
Returns all published featured blogs.

**Response Format:**

**On Success** - Return HTTP status 200. Also return the blog documents.
```json
[
  {
    "id": 1,
    "title": "Hair Care Tips",
    "excerpt": "Essential tips for maintaining healthy hair...",
    "image": "https://res.cloudinary.com/...",
    "category": "Hair Care",
    "readTime": "5 min read",
    "content": "Full blog content...",
    "createdAt": "2024-01-10T06:25:46.051Z",
    "updatedAt": "2024-01-10T06:25:46.051Z",
    "isFeatured": true,
    "status": "PUBLISHED"
  }
]
```

**Status Codes:**
- `200` - Success

---

### Blog APIs (Authentication and Authorization required - Admin only)

#### GET /api/blogs/admin/all
Returns all blogs in the collection (including drafts).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return all blog documents.
```json
[
  {
    "id": 1,
    "title": "Hair Care Tips",
    "excerpt": "Essential tips for maintaining healthy hair...",
    "image": "https://res.cloudinary.com/...",
    "category": "Hair Care",
    "readTime": "5 min read",
    "content": "Full blog content...",
    "createdAt": "2024-01-10T06:25:46.051Z",
    "updatedAt": "2024-01-10T06:25:46.051Z",
    "isFeatured": true,
    "status": "PUBLISHED"
  }
]
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### POST /api/blogs/admin
Create a blog document from request body.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "title": "Hair Care Tips",
  "excerpt": "Essential tips for maintaining healthy hair...",
  "image": "https://res.cloudinary.com/...",
  "category": "Hair Care",
  "readTime": "5 min read",
  "content": "Full blog content...",
  "isFeatured": false,
  "status": "PUBLISHED"
}
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return the blog document.
```json
{
  "id": 1,
  "title": "Hair Care Tips",
  "excerpt": "Essential tips for maintaining healthy hair...",
  "image": "https://res.cloudinary.com/...",
  "category": "Hair Care",
  "readTime": "5 min read",
  "content": "Full blog content...",
  "createdAt": "2024-01-10T06:25:46.051Z",
  "updatedAt": "2024-01-10T06:25:46.051Z",
  "isFeatured": false,
  "status": "PUBLISHED"
}
```

**Note:** If blog status is set to "PUBLISHED", an email notification is automatically sent to all active newsletter subscribers.

**Status Codes:**
- `200` - Success
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### PUT /api/blogs/admin/{id}
Updates a blog by changing at least one or all fields.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id` - Blog ID (number)

**Request Body:**
```json
{
  "title": "Updated Hair Care Tips",
  "excerpt": "Updated excerpt...",
  "image": "https://res.cloudinary.com/...",
  "category": "Hair Care",
  "readTime": "7 min read",
  "content": "Updated content...",
  "isFeatured": true,
  "status": "PUBLISHED"
}
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return the updated blog document.
```json
{
  "id": 1,
  "title": "Updated Hair Care Tips",
  "excerpt": "Updated excerpt...",
  "image": "https://res.cloudinary.com/...",
  "category": "Hair Care",
  "readTime": "7 min read",
  "content": "Updated content...",
  "createdAt": "2024-01-10T06:25:46.051Z",
  "updatedAt": "2024-01-10T08:47:15.297Z",
  "isFeatured": true,
  "status": "PUBLISHED"
}
```

**Note:** If status changes from non-published to "PUBLISHED", an email notification is automatically sent to all active newsletter subscribers.

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `404` - Blog not found
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### DELETE /api/blogs/admin/{id}
Deletes a blog by blog id.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id` - Blog ID (number)

**Response Format:**

**On Success** - Return HTTP status 200.

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `404` - Blog not found
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

## FEATURE III - Booking Management

### Models

#### Booking Model
```json
{
  "id": {number, auto-generated},
  "name": {string, mandatory},
  "email": {string, mandatory, valid email},
  "phone": {string, mandatory},
  "serviceName": {string, mandatory},
  "date": {string, mandatory},
  "time": {string, mandatory},
  "message": {string, optional, max length 2000},
  "createdAt": {timestamp, auto-generated},
  "isRead": {boolean, default: false}
}
```

---

### Booking APIs

#### POST /api/bookings (No authentication required)
Create a booking document from request body. Sends confirmation emails and creates a lead in TeleCRM.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "9876543210",
  "serviceName": "Hair Cut",
  "date": "2024-01-15",
  "time": "10:00 AM",
  "message": "Looking forward to the appointment"
}
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return the booking document.
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "9876543210",
  "serviceName": "Hair Cut",
  "date": "2024-01-15",
  "time": "10:00 AM",
  "message": "Looking forward to the appointment",
  "createdAt": "2024-01-10T06:25:46.051Z",
  "isRead": false
}
```

**Note:** 
- Confirmation emails are sent to both customer and admin
- A lead is automatically created in TeleCRM system

**Status Codes:**
- `200` - Success
- `400` - Bad Request

---

### Booking APIs (Authentication and Authorization required - Admin only)

#### GET /api/bookings/admin/all
Returns all bookings in the collection, ordered by creation date (newest first).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return the booking documents.
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "9876543210",
    "serviceName": "Hair Cut",
    "date": "2024-01-15",
    "time": "10:00 AM",
    "message": "Looking forward to the appointment",
    "createdAt": "2024-01-10T06:25:46.051Z",
    "isRead": false
  }
]
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### GET /api/bookings/admin/unread
Returns all unread bookings, ordered by creation date (newest first).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return the unread booking documents.
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "9876543210",
    "serviceName": "Hair Cut",
    "date": "2024-01-15",
    "time": "10:00 AM",
    "message": "Looking forward to the appointment",
    "createdAt": "2024-01-10T06:25:46.051Z",
    "isRead": false
  }
]
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### PUT /api/bookings/admin/{id}/mark-read
Marks a booking as read.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id` - Booking ID (number)

**Response Format:**

**On Success** - Return HTTP status 200. Also return the updated booking document.
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "9876543210",
  "serviceName": "Hair Cut",
  "date": "2024-01-15",
  "time": "10:00 AM",
  "message": "Looking forward to the appointment",
  "createdAt": "2024-01-10T06:25:46.051Z",
  "isRead": true
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `404` - Booking not found
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### DELETE /api/bookings/admin/{id}
Deletes a booking by booking id.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id` - Booking ID (number)

**Response Format:**

**On Success** - Return HTTP status 200.

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `404` - Booking not found
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

## FEATURE IV - Newsletter Management

### Models

#### NewsletterSubscriber Model
```json
{
  "id": {number, auto-generated},
  "email": {string, mandatory, unique, valid email},
  "isActive": {boolean, default: true},
  "subscribedAt": {timestamp, auto-generated}
}
```

---

### Newsletter APIs (No authentication required for subscription)

#### POST /api/newsletter/subscribe
Subscribe an email to the newsletter. Sends a welcome email.

**Request Body:**
```json
{
  "email": "subscriber@example.com"
}
```

**Response Format:**

**On Success** - Return HTTP status 200.
```json
{
  "success": true,
  "message": "Successfully subscribed to newsletter"
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.
```json
{
  "success": false,
  "message": "This email is already subscribed"
}
```

**Note:** 
- If email already exists but is inactive, it will be reactivated
- Welcome email is automatically sent to the subscriber

**Status Codes:**
- `200` - Success
- `400` - Bad Request (email already subscribed, invalid email)

---

### Newsletter APIs (Authentication and Authorization required - Admin only)

#### GET /api/newsletter/admin/all
Returns all newsletter subscribers, ordered by subscription date (newest first).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return the subscriber documents.
```json
[
  {
    "id": 1,
    "email": "subscriber@example.com",
    "isActive": true,
    "subscribedAt": "2024-01-10T06:25:46.051Z"
  }
]
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### GET /api/newsletter/admin/active
Returns all active newsletter subscribers.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response Format:**

**On Success** - Return HTTP status 200. Also return the active subscriber documents.
```json
[
  {
    "id": 1,
    "email": "subscriber@example.com",
    "isActive": true,
    "subscribedAt": "2024-01-10T06:25:46.051Z"
  }
]
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### POST /api/newsletter/admin/add
Manually add a subscriber to the newsletter (admin only).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "email": "subscriber@example.com"
}
```

**Response Format:**

**On Success** - Return HTTP status 200.
```json
{
  "success": true,
  "message": "Subscriber added successfully",
  "subscriber": {
    "id": 1,
    "email": "subscriber@example.com",
    "isActive": true,
    "subscribedAt": "2024-01-10T06:25:46.051Z"
  }
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `400` - Bad Request (email already exists)
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### DELETE /api/newsletter/admin/{id}
Deletes a subscriber from the newsletter.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id` - Subscriber ID (number)

**Response Format:**

**On Success** - Return HTTP status 200.
```json
{
  "success": true,
  "message": "Subscriber deleted successfully"
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `404` - Subscriber not found
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### PUT /api/newsletter/admin/{id}/deactivate
Deactivates a subscriber (marks as inactive without deleting).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id` - Subscriber ID (number)

**Response Format:**

**On Success** - Return HTTP status 200.
```json
{
  "success": true,
  "message": "Subscriber deactivated successfully",
  "subscriber": {
    "id": 1,
    "email": "subscriber@example.com",
    "isActive": false,
    "subscribedAt": "2024-01-10T06:25:46.051Z"
  }
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `404` - Subscriber not found
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

#### PUT /api/newsletter/admin/{id}/activate
Activates a previously deactivated subscriber.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id` - Subscriber ID (number)

**Response Format:**

**On Success** - Return HTTP status 200.
```json
{
  "success": true,
  "message": "Subscriber activated successfully",
  "subscriber": {
    "id": 1,
    "email": "subscriber@example.com",
    "isActive": true,
    "subscribedAt": "2024-01-10T06:25:46.051Z"
  }
}
```

**On Error** - Return a suitable error message with a valid HTTP status code.

**Status Codes:**
- `200` - Success
- `404` - Subscriber not found
- `401` - Unauthorized
- `403` - Forbidden (not admin)

---

## Common Error Responses

### 400 Bad Request
```json
{
  "error": "Error message describing what went wrong"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized"
}
```

### 403 Forbidden
```json
{
  "error": "Access denied"
}
```

### 404 Not Found
```json
{
  "error": "Resource not found"
}
```

---

## CORS Configuration

The API allows requests from the following origins:
- `http://localhost:3001` (Development)
- `https://lumiereluxe.in` (Production)
- `https://www.lumiereluxe.in` (Production with www)

---

## Notes

1. **Password Encryption**: All passwords are encrypted using bcrypt before storage.

2. **JWT Tokens**: JWT tokens contain the username and are used for authentication. Tokens should be included in the Authorization header as `Bearer <token>`.

3. **Email Notifications**:
   - Blog notifications are sent to all active newsletter subscribers when a blog is published
   - Booking confirmations are sent to both customer and admin
   - Welcome emails are sent to new newsletter subscribers

4. **TeleCRM Integration**: Booking submissions automatically create leads in the TeleCRM system.

5. **Image Storage**: Blog images are stored using Cloudinary or S3. The image URL is stored in the database.

6. **Database**: The application uses MySQL database with JPA/Hibernate for ORM.

7. **Session Management**: The application uses stateless JWT authentication (no server-side sessions).

---

## API Testing Examples

### Register Admin
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

### Get All Published Blogs
```bash
curl -X GET http://localhost:8081/api/blogs/public/all
```

### Create Booking
```bash
curl -X POST http://localhost:8081/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "serviceName": "Hair Cut",
    "date": "2024-01-15",
    "time": "10:00 AM",
    "message": "Looking forward to the appointment"
  }'
```

### Subscribe to Newsletter
```bash
curl -X POST http://localhost:8081/api/newsletter/subscribe \
  -H "Content-Type: application/json" \
  -d '{
    "email": "subscriber@example.com"
  }'
```

### Create Blog (Admin)
```bash
curl -X POST http://localhost:8081/api/blogs/admin \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "title": "Hair Care Tips",
    "excerpt": "Essential tips for maintaining healthy hair...",
    "image": "https://res.cloudinary.com/...",
    "category": "Hair Care",
    "readTime": "5 min read",
    "content": "Full blog content...",
    "isFeatured": false,
    "status": "PUBLISHED"
  }'
```

