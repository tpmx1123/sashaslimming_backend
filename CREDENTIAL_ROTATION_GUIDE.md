# Credential Rotation Guide

## Steps to Rotate Exposed SMTP Credentials

### 1. Generate New Gmail App Password (if using Gmail)

1. Go to your Google Account: https://myaccount.google.com/
2. Navigate to **Security** → **2-Step Verification** → **App passwords**
3. Generate a new app password for "Mail"
4. Copy the 16-character password

### 2. Update Environment Variables

**Local Development:**
```bash
# Update your .env file
MAIL_PASSWORD=your_new_app_password_here
```

**Production/Server:**
- Update environment variables on your hosting platform
- For example, if using a service like Heroku, AWS, or DigitalOcean:
  ```bash
  # Example for Heroku
  heroku config:set MAIL_PASSWORD=your_new_app_password_here
  
  # Example for Linux server
  export MAIL_PASSWORD=your_new_app_password_here
  ```

### 3. Restart Your Application

After updating the environment variables, restart your Spring Boot application to load the new credentials.

### 4. Test Email Functionality

Verify that emails are still being sent correctly with the new credentials.

### 5. Remove Old Credentials from Git History

**⚠️ WARNING: This rewrites git history. Coordinate with your team before doing this.**

```bash
# Option 1: Using git-filter-repo (recommended)
pip install git-filter-repo
git filter-repo --path backend/src/main/resources/application.properties --invert-paths
git push origin --force --all

# Option 2: Using BFG Repo-Cleaner
# Download from: https://rtyley.github.io/bfg-repo-cleaner/
java -jar bfg.jar --replace-text passwords.txt
git reflog expire --expire=now --all
git gc --prune=now --aggressive
git push origin --force --all
```

**Note:** After rewriting history, all team members will need to:
```bash
git fetch origin
git reset --hard origin/main  # or origin/master
```

### 6. Verify No Credentials in Current Code

```bash
# Search for potential hardcoded credentials
grep -r "password.*=" backend/src --exclude-dir=target
grep -r "MAIL_PASSWORD.*=" backend/src --exclude-dir=target
```

All results should show only `${MAIL_PASSWORD}` (environment variable syntax), not actual passwords.

### 7. Enable GitGuardian Alerts

- Set up GitGuardian to scan your repository automatically
- Configure alerts to notify you immediately if credentials are detected
- Review and fix any alerts promptly

## Prevention for Future

1. **Pre-commit Hooks**: Install git-secrets or similar tools
2. **Code Review**: Always review changes to configuration files
3. **Automated Scanning**: Use GitGuardian or similar services
4. **Documentation**: Keep this guide updated

