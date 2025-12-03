# Environment Variables Setup Guide

## Quick Start

1. **Copy the example file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` file** with your actual credentials:
   ```env
   DB_URL=jdbc:mysql://localhost:3306/saloondb
   DB_USERNAME=root
   DB_PASSWORD=your_actual_password
   JWT_SECRET=your_jwt_secret
   JWT_EXPIRATION=86400000
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your_email@gmail.com
   MAIL_PASSWORD=your_app_password
   ```

3. **Make sure `.env` is in the `backend/` directory** (same level as `pom.xml`)

4. **Run the application:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

## How It Works

- The application uses **dotenv-java** to load variables from `.env` file
- Variables are loaded **before** Spring Boot starts
- Spring Boot reads them via `${VAR}` syntax in `application.properties`
- Priority order:
  1. System properties (set via `-D` flag)
  2. Environment variables (set in OS)
  3. `.env` file values
  4. Default values (if any)

## Important Notes

- ✅ `.env` file is in `.gitignore` (won't be committed)
- ✅ `.env.example` is a template (safe to commit)
- ✅ Never commit your actual `.env` file
- ✅ The application will work even if `.env` is missing (uses system env vars)

## Troubleshooting

**Error: "Could not resolve placeholder 'MAIL_HOST'"**
- Make sure `.env` file exists in `backend/` directory
- Check that all required variables are in `.env`
- Verify variable names match exactly (case-sensitive)

**Variables not loading:**
- Ensure `.env` file is in the correct location (backend folder)
- Check for typos in variable names
- Restart the application after creating/editing `.env`

