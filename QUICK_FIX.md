# 🚨 URGENT: Fix Exposed SMTP Credentials

## Immediate Action Required (5 minutes)

### Step 1: Rotate Your Email Password NOW
1. Go to your email provider (Gmail, etc.)
2. Generate a **NEW** App Password or change your password
3. Update the `MAIL_PASSWORD` environment variable on your server/hosting

### Step 2: Verify Current Code is Safe
✅ Your `application.properties` is already secure - it uses `${MAIL_PASSWORD}` (environment variable)
✅ No hardcoded credentials in current code

### Step 3: The Problem
⚠️ **The exposed credentials are in your git history** (from November 10th, 2025)
- Even though current code is safe, the old commit still has the password
- Anyone with access to your repository can see the old commit

### Step 4: Remove from Git History (After rotating password)

**Option A: Quick Fix (if you're the only developer)**
```bash
# This rewrites history - coordinate with team first!
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch backend/src/main/resources/application.properties" \
  --prune-empty --tag-name-filter cat -- --all

git push origin --force --all
```

**Option B: Safer Approach (recommended)**
1. Rotate credentials first (Step 1)
2. Use GitGuardian's remediation guide
3. Or use BFG Repo-Cleaner tool

### Step 5: Prevent Future Issues
- ✅ Never commit `.env` files
- ✅ Always use environment variables
- ✅ Review `SECURITY.md` for best practices
- ✅ Set up GitGuardian alerts

## Current Status
- ✅ Code is secure (using env vars)
- ⚠️ Git history contains old credentials
- 🔄 Need to rotate password
- 🔄 Need to clean git history

## Need Help?
See `CREDENTIAL_ROTATION_GUIDE.md` for detailed steps.

