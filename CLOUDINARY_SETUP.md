# Cloudinary Image Upload Setup

## Environment Variables

Add these to your `backend/.env` file (lines 32-34):

```env
CLOUDINARY_CLOUD_NAME=di4caiech
CLOUDINARY_API_KEY=your_api_key_here
CLOUDINARY_API_SECRET=your_api_secret_here
```

## Troubleshooting ERR_CONNECTION_REFUSED

This error means the frontend cannot connect to the backend server. Check the following:

### 1. Backend Server Status
Make sure the backend is running:
```bash
cd backend
mvn spring-boot:run
```

You should see:
```
Started SlimmingApplication in X.XXX seconds
```

### 2. Verify Backend Port
The backend runs on port **8081** (configured in `application.properties`).

Check if the port is in use:
- Windows: `netstat -ano | findstr :8081`
- Linux/Mac: `lsof -i :8081`

### 3. Check Backend Logs
Look for errors during startup:
- Database connection issues
- Missing environment variables
- Cloudinary configuration errors

### 4. Test Backend Endpoint
Open browser or use curl:
```bash
curl http://localhost:8081/api/blogs/public/all
```

If this fails, the backend is not running or not accessible.

### 5. Frontend Configuration
The frontend is configured to connect to:
```
http://localhost:8081/api
```

Make sure:
- Frontend is running (usually on port 3000, 3001, or 5173)
- Backend is running on port 8081
- No firewall blocking the connection

### 6. CORS Configuration
The backend allows these origins:
- http://localhost:3000
- http://localhost:3001
- http://localhost:5173

Make sure your frontend runs on one of these ports.

## Image Upload Endpoint

**Endpoint:** `POST /api/blogs/admin/upload-image`

**Authentication:** Required (Admin role)

**Request:**
- Content-Type: `multipart/form-data`
- Parameter: `file` (image file)

**Response:**
```json
{
  "url": "https://res.cloudinary.com/di4caiech/image/upload/...",
  "success": "true"
}
```

## Testing the Upload

1. Start backend: `cd backend && mvn spring-boot:run`
2. Start frontend: `cd frontend && npm start` (or `npm run dev`)
3. Login to admin panel
4. Go to blog form (create or edit)
5. Click "Upload" button next to Featured Image URL
6. Select an image file
7. Wait for upload to complete
8. The URL field should auto-populate with Cloudinary URL

