# TeleCRM Integration Setup Guide

## ✅ Integration Complete!

The TeleCRM integration has been successfully added to your booking system. All booking submissions will now automatically create leads in TeleCRM.

## 🔐 Environment Variables Required

You need to set this environment variable in your deployment:

```bash
TELECRM_API_TOKEN=your_api_token_here
```

**Note:** The Enterprise ID (`687f3db724643aa89dbe3b20`) is now configured in `application.properties`. The API URL is: `https://next-api.telecrm.in/enterprise/687f3db724643aa89dbe3b20/autoupdatelead`

## 📋 Files Created/Modified

### Created Files:
1. `src/main/java/com/example/saloon/service/TeleCRMService.java` - TeleCRM API integration service
2. `src/main/java/com/example/saloon/config/RestTemplateConfig.java` - HTTP client configuration

### Modified Files:
1. `src/main/resources/application.properties` - Added TeleCRM configuration
2. `src/main/java/com/example/saloon/controller/BookingController.java` - Added TeleCRM service call

## 🚀 How It Works

1. User submits booking via website
2. Booking is saved to database
3. Confirmation emails are sent (existing functionality)
4. **NEW:** Lead is automatically sent to TeleCRM

## 🧪 Testing

1. Make sure environment variables are set
2. Submit a test booking from your website
3. Check TeleCRM dashboard - you should see the new lead
4. Check backend logs for success/error messages:
   - 🔵 `TeleCRM createLead called for booking: [name]` - Service method was called
   - 🔵 `Calling TeleCRM service for booking: [name]` - Controller is calling the service
   - ✅ `TeleCRM lead created successfully for: [name]` - Success message
   - ❌ `Failed to send lead to TeleCRM: [error]` - Error message
   - ❌ `TELECRM_API_TOKEN is not set or is empty!` - Token missing
   - ❌ `TeleCRMService is NULL - not injected!` - Service injection failed

## 📝 TeleCRM Field Mapping

The following fields are sent to TeleCRM (matching your form):
- `name` - Customer name
- `phone` - Phone number (Lead ID - automatically formatted with +91 if needed)
- `email` - Email address
- `appointment_date_and_time` - Combined date and time in format "DD/MM/YYYY HH:mm:ss"
- `note` - Additional message/notes (if provided)
- `lead_source` - Set to "lumiereluxeweb" to track bookings from website
- `client_concerns` - Service name mapped to TeleCRM dropdown values (e.g., "hair-cut" → "Hair Cut")

**Important:** The field names must match exactly what's in your TeleCRM workspace. If your TeleCRM uses different field names (e.g., different casing or underscores), update them in `TeleCRMService.java`.

**Field Name Variations to Check:**
- Appointment field might be: `appointment_date_and_time`, `appointmentDateAndTime`, or `appointment_date_time`
- Note field might be: `note`, `notes`, or `message`
- Lead source might be: `lead_source`, `leadSource`, or `leadSource`
- Client concerns might be: `client_concerns`, `clientConcerns`, or `client_concern`

## ⚠️ Important Notes

- TeleCRM integration is **fire-and-forget** - if it fails, the booking still succeeds
- Phone numbers are automatically formatted to include country code (+91)
- All errors are logged but don't affect the booking process
- The integration uses Bearer token authentication

## 🔧 Troubleshooting

If leads aren't appearing in TeleCRM:

1. **Check environment variables** - Make sure they're set correctly
2. **Check backend logs** - Look for error messages
3. **Verify field names** - Ensure field names in TeleCRM match the code
4. **Test API token** - Verify the token is valid in TeleCRM dashboard
5. **Check enterprise ID** - Confirm the ID is correct

### Fixing "N/A" in CLIENT CONCERNS Field

If CLIENT CONCERNS shows "N/A" in TeleCRM:

1. **Check the logs** - Look for `🔵 Client Concerns value:` to see what value is being sent
2. **Check TeleCRM dropdown options** - In TeleCRM, open the CLIENT CONCERNS field settings and see the exact dropdown values
3. **Update the mapping** - In `TeleCRMService.java`, find the `mapServiceToClientConcerns()` method and update the mappings to match EXACTLY what's in your TeleCRM dropdown
4. **Check field name** - The field name might be different. Try uncommenting these lines in the code:
   - `fields.put("clientConcerns", clientConcerns);` (camelCase)
   - `fields.put("client_concern", clientConcerns);` (singular)
5. **Verify exact match** - The value must match EXACTLY (case-sensitive, including spaces, ampersands, etc.)

## 📞 Support

If you encounter issues, check:
- Backend application logs
- TeleCRM API documentation: https://docs.telecrm.in
- Network connectivity to `https://next-api.telecrm.in`

