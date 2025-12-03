# Security Notice - Exposed Credentials

## ⚠️ CRITICAL: SMTP Credentials Exposed

GitGuardian has detected SMTP credentials that were exposed in the git history (pushed on November 10th, 2025).

### Immediate Actions Required:

1. **ROTATE ALL EXPOSED CREDENTIALS IMMEDIATELY**
   - Change your Gmail/email account password
   - Generate a new Gmail App Password (if using Gmail)
   - Update the `MAIL_PASSWORD` environment variable with the new password

2. **Remove Credentials from Git History**
   ```bash
   # Use git-filter-repo or BFG Repo-Cleaner to remove sensitive data
   # Example with git-filter-repo:
   git filter-repo --path backend/src/main/resources/application.properties --invert-paths
   # Then force push (WARNING: This rewrites history)
   git push origin --force --all
   ```

3. **Verify Current Configuration**
   - Ensure `application.properties` only contains environment variable placeholders
   - Never commit actual credentials to the repository
   - Use environment variables or secure secret management

### Current Secure Configuration

The `application.properties` file now uses environment variables:
- `MAIL_HOST=${MAIL_HOST}`
- `MAIL_USERNAME=${MAIL_USERNAME}`
- `MAIL_PASSWORD=${MAIL_PASSWORD}`

### Setting Environment Variables

**For Local Development:**
- Create a `.env` file (already in .gitignore)
- Copy `.env.example` to `.env`
- Fill in your actual credentials in `.env`
- Use a tool like `dotenv-java` or set system environment variables

**For Production:**
- Set environment variables on your hosting platform
- Use secret management services (AWS Secrets Manager, Azure Key Vault, etc.)
- Never hardcode credentials in any configuration files

### Best Practices

1. ✅ Always use environment variables for sensitive data
2. ✅ Keep `.env` files in `.gitignore`
3. ✅ Use `.env.example` as a template (without real values)
4. ✅ Regularly rotate credentials
5. ✅ Use secret scanning tools (like GitGuardian) to detect leaks
6. ❌ Never commit passwords, API keys, or secrets to git

