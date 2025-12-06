package com.example.slimming.service;

import com.example.slimming.entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class TeleCRMService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${telecrm.api.url}")
    private String telecrmApiUrl;

    @Value("${telecrm.enterprise.id}")
    private String enterpriseId;

    @Value("${telecrm.api.token}")
    private String apiToken;

    private static final DateTimeFormatter TELECRM_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Sends booking data to TeleCRM as a lead
     * This is fire-and-forget - errors are logged but don't affect booking
     */
    public void createLead(Booking booking) {
        // Log that method was called
        System.out.println("🔵 TeleCRM createLead called for booking: " + booking.getName());
        
        // Check if token is set
        if (apiToken == null || apiToken.trim().isEmpty()) {
            System.err.println("❌ TELECRM_API_TOKEN is not set or is empty!");
            return;
        }
        
        // Log configuration (without exposing full token)
        System.out.println("🔵 TeleCRM URL: " + telecrmApiUrl);
        System.out.println("🔵 TeleCRM Enterprise ID: " + enterpriseId);
        System.out.println("🔵 TeleCRM Token present: " + (apiToken != null && !apiToken.isEmpty()));
        
        try {
            String url = telecrmApiUrl + "/enterprise/" + enterpriseId + "/autoupdatelead";
            System.out.println("🔵 TeleCRM Full URL: " + url);

            // Build the payload
            Map<String, Object> payload = new HashMap<>();
            
            // Fields object - must include phone (unique identifier/Lead ID)
            Map<String, Object> fields = new HashMap<>();
            fields.put("name", booking.getName());
            fields.put("phone", formatPhoneNumber(booking.getPhone())); // Lead ID field
            fields.put("email", booking.getEmail());
            
            // Combine date and time into single field: "DD/MM/YYYY HH:mm:ss"
            String appointmentDateTime = formatAppointmentDateTime(booking.getDate(), booking.getTime());
            fields.put("appointment_date_and_time", appointmentDateTime);
            
            // Add note (message) if present
            if (booking.getMessage() != null && !booking.getMessage().trim().isEmpty()) {
                fields.put("note", booking.getMessage());
            }
            
            // Set lead source to match TeleCRM dropdown value
            // Try multiple field name variations to ensure it works
            // TeleCRM might use different field names: lead_source, leadSource, source, lead_source_name
            String leadSourceValue = "sashaslimmingweb";
            fields.put("lead_source", leadSourceValue);  // snake_case (most common)
            fields.put("leadSource", leadSourceValue);   // camelCase (alternative)
            fields.put("source", leadSourceValue);       // simple name (alternative)
            System.out.println("🔵 TeleCRM Lead Source value: " + leadSourceValue);
            
            // Add service name to CLIENT CONCERNS field
            // Note: If TeleCRM shows "N/A", the field name or value might not match exactly
            // Try these field name variations: "client_concerns", "clientConcerns", "client_concern"
            if (booking.getServiceName() != null && !booking.getServiceName().trim().isEmpty()) {
                String clientConcerns = mapServiceToClientConcerns(booking.getServiceName());
                System.out.println("🔵 Service: " + booking.getServiceName() + " -> Client Concerns: " + clientConcerns);
                
                // Try multiple field name variations (TeleCRM might use different naming)
                fields.put("client_concerns", clientConcerns);  // snake_case
                // Uncomment if needed:
                // fields.put("clientConcerns", clientConcerns);  // camelCase
                // fields.put("client_concern", clientConcerns);  // singular
            }
            
            payload.put("fields", fields);
            
            // Log the payload being sent (for debugging)
            System.out.println("🔵 TeleCRM Payload: " + payload.toString());
            System.out.println("🔵 Client Concerns value: " + fields.get("client_concerns"));

            // Actions array - record the booking action (optional)
            // Commented out - uncomment if you need to track actions in TeleCRM
            /*
            Map<String, Object> action = new HashMap<>();
            action.put("type", "ACTION_1001"); // Custom action type - adjust if needed
            Map<String, Object> actionFields = new HashMap<>();
            actionFields.put("service", booking.getServiceName());
            action.put("fields", actionFields);
            
            // Format created_on as DD/MM/YYYY HH:mm:ss
            String createdOn = booking.getCreatedAt() != null 
                ? booking.getCreatedAt().format(TELECRM_DATE_FORMAT)
                : LocalDateTime.now().format(TELECRM_DATE_FORMAT);
            action.put("created_on", createdOn);
            
            payload.put("actions", new Object[]{action});
            */

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ TeleCRM lead created successfully for: " + booking.getName());
                System.out.println("🔵 TeleCRM Response: " + response.getBody());
            } else {
                System.err.println("⚠️ TeleCRM API returned status: " + response.getStatusCode());
                System.err.println("⚠️ TeleCRM Response: " + response.getBody());
            }

        } catch (Exception e) {
            // Log error but don't fail the booking
            System.err.println("❌ Failed to send lead to TeleCRM: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - booking should succeed even if TeleCRM fails
        }
    }

    /**
     * Formats phone number to include country code if missing
     * TeleCRM expects phone with country code (e.g., +91...)
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        
        String cleaned = phone.trim().replaceAll("[\\s-()]", "");
        
        // If already starts with +, return as is
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        
        // If starts with 91 (India country code without +), add +
        if (cleaned.startsWith("91") && cleaned.length() >= 12) {
            return "+" + cleaned;
        }
        
        // If starts with 0, remove 0 and add +91
        if (cleaned.startsWith("0")) {
            return "+91" + cleaned.substring(1);
        }
        
        // If 10 digits, assume Indian number and add +91
        if (cleaned.length() == 10 && cleaned.matches("\\d{10}")) {
            return "+91" + cleaned;
        }
        
        // Default: add +91 if it's all digits
        if (cleaned.matches("\\d+")) {
            return "+91" + cleaned;
        }
        
        return phone; // Return original if can't format
    }

    /**
     * Maps service name from booking form to TeleCRM CLIENT CONCERNS dropdown value
     * Since CLIENT CONCERNS is a dropdown, we need to send exact values that match TeleCRM options
     * 
     * IMPORTANT: If TeleCRM shows "N/A" for CLIENT CONCERNS:
     * 1. Check the logs to see what value we're sending (look for "🔵 Client Concerns value:")
     * 2. In TeleCRM, check the dropdown options for CLIENT CONCERNS field
     * 3. The value must match EXACTLY (case-sensitive, including spaces and special characters)
     * 4. Update the mappings below to match TeleCRM's exact dropdown values
     * 5. If TeleCRM uses IDs/codes instead of display text, you may need to map to those instead
     */
    private String mapServiceToClientConcerns(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return null;
        }
        
        // Map our service values to TeleCRM CLIENT CONCERNS dropdown values
        // IMPORTANT: These values must match EXACTLY what's in your TeleCRM dropdown
        // To find the correct values:
        // 1. Go to TeleCRM and check the CLIENT CONCERNS dropdown options
        // 2. Copy the exact text/value from each option
        // 3. Update the mappings below to match exactly
        Map<String, String> serviceMapping = new HashMap<>();
        
        // Slimming Services - Using "slimming-" prefix as requested
        // Format: "slimming-Consultation", "slimming-fat-reduction", etc.
        serviceMapping.put("consultation", "slimming-Consultation");
        serviceMapping.put("fat-reduction", "slimming-fat-reduction");
        serviceMapping.put("inch-loss", "slimming-inch-loss");
        serviceMapping.put("muscle-toning", "slimming-muscle-toning");
        serviceMapping.put("skin-tightening", "slimming-skin-tightening");
        serviceMapping.put("surgical-sculpting", "slimming-surgical-sculpting");
        
        // Return mapped value, or format as fallback
        String mappedValue = serviceMapping.get(serviceName.toLowerCase().trim());
        if (mappedValue != null) {
            return mappedValue;
        }
        
        // Fallback: format the service name if no mapping found
        System.err.println("⚠️ No mapping found for service: " + serviceName + ", using formatted value");
        String formatted = formatServiceName(serviceName);
        return formatted != null ? "slimming-" + formatted : null;
    }
    
    /**
     * Formats service name to a more readable format (fallback method)
     * Converts kebab-case to Title Case (e.g., "hair-cut" -> "Hair Cut")
     */
    private String formatServiceName(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return serviceName;
        }
        
        // Replace hyphens with spaces and capitalize words
        String[] words = serviceName.split("-");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                // Capitalize first letter and lowercase the rest
                String word = words[i].substring(0, 1).toUpperCase() + 
                             words[i].substring(1).toLowerCase();
                if (i > 0) {
                    formatted.append(" ");
                }
                formatted.append(word);
            }
        }
        
        return formatted.toString();
    }

    /**
     * Combines date and time into TeleCRM format: "DD/MM/YYYY HH:mm:ss"
     * Input date format: "YYYY-MM-DD" (from HTML date input)
     * Input time format: "HH:mm" (from HTML time input)
     */
    private String formatAppointmentDateTime(String date, String time) {
        try {
            // Parse date from "YYYY-MM-DD" to "DD/MM/YYYY"
            String[] dateParts = date.split("-");
            if (dateParts.length == 3) {
                String formattedDate = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
                // Combine with time: "DD/MM/YYYY HH:mm:ss"
                return formattedDate + " " + time + ":00";
            }
        } catch (Exception e) {
            System.err.println("Error formatting appointment date/time: " + e.getMessage());
        }
        // Fallback: return current date/time if parsing fails
        return LocalDateTime.now().format(TELECRM_DATE_FORMAT);
    }
}

