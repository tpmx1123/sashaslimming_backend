package com.example.slimming.service;

import com.example.slimming.entity.Blog;
import com.example.slimming.entity.Booking;
import com.example.slimming.entity.NewsletterSubscriber;
import com.example.slimming.repository.NewsletterSubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NewsletterSubscriberRepository subscriberRepository;

    @Value("${spring.mail.username:noreply@slimming.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:1573}")
    private String frontendUrl;

    @Value("${app.contact.email:hello@sashaclinics.com}")
    private String contactEmail;

    private static final String FROM_NAME = "Slimming";

    public void sendBlogNotification(Blog blog) {
        List<NewsletterSubscriber> activeSubscribers = subscriberRepository.findByIsActiveTrue();
        
        if (activeSubscribers.isEmpty()) {
            return;
        }

        String blogUrl = frontendUrl + "/blog/" + blog.getId();
        
        for (NewsletterSubscriber subscriber : activeSubscribers) {
            try {
                sendBlogEmail(subscriber.getEmail(), blog, blogUrl);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + subscriber.getEmail() + ": " + e.getMessage());
                // Continue with other subscribers even if one fails
            }
        }
    }

    private void sendBlogEmail(String toEmail, Blog blog, String blogUrl) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, FROM_NAME);
            helper.setTo(toEmail);
            helper.setSubject("New Blog Post: " + blog.getTitle());

            String htmlContent = buildEmailTemplate(blog, blogUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Encoding error: " + e.getMessage(), e);
        }
    }

    private String buildEmailTemplate(Blog blog, String blogUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color:rgb(144, 58, 143); color: white; padding: 20px; text-align: center; }" +
                ".content { background-color: #f9f9f9; padding: 20px; }" +
                ".blog-image { width: 100%; max-width: 560px; height: auto; margin: 20px 0; }" +
                ".button { display: inline-block; padding: 12px 24px; background-color:rgb(95, 49, 96); color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>New Blog Post Available!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>" + blog.getTitle() + "</h2>" +
                "<p><strong>Category:</strong> " + blog.getCategory() + "</p>" +
                "<img src='" + blog.getImage() + "' alt='" + blog.getTitle() + "' class='blog-image' />" +
                "<p>" + blog.getExcerpt() + "</p>" +
                "<a href='" + blogUrl + "' class='button'>Read Full Article</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Thank you for subscribing to our newsletter!</p>" +
                "<p>If you no longer wish to receive these emails, please contact us.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public void sendWelcomeEmail(String email) {
        try {
            // Use MimeMessage to support display name
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, FROM_NAME);
            helper.setTo(email);
            helper.setSubject("Welcome to Our Newsletter!");
            helper.setText("Thank you for subscribing to our newsletter! You'll be the first to know about new blog posts, beauty tips, and exclusive offers.", false);
            
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Failed to send welcome email to " + email + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to " + email + ": " + e.getMessage());
        }
    }

    public void sendBookingConfirmation(Booking booking) {
        try {
            sendBookingConfirmationToCustomer(booking);
            sendBookingNotificationToAdmin(booking);
        } catch (Exception e) {
            System.err.println("Failed to send booking emails: " + e.getMessage());
        }
    }

    private void sendBookingConfirmationToCustomer(Booking booking) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, FROM_NAME);
            helper.setTo(booking.getEmail());
            helper.setSubject("Booking Confirmation - " + booking.getServiceName());

            String htmlContent = buildBookingConfirmationTemplate(booking);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Encoding error: " + e.getMessage(), e);
        }
    }

    private void sendBookingNotificationToAdmin(Booking booking) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Use customer's name as display name for the sender
            String customerName = booking.getName() != null && !booking.getName().trim().isEmpty() 
                ? booking.getName() 
                : "Customer";
            helper.setFrom(fromEmail, customerName);
            helper.setTo(fromEmail); // Send to admin email (same as fromEmail)
            helper.setSubject("New Booking Request - " + booking.getServiceName());

            String htmlContent = buildAdminNotificationTemplate(booking);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Encoding error: " + e.getMessage(), e);
        }
    }

    private String buildBookingConfirmationTemplate(Booking booking) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color:rgb(144, 58, 144); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 30px; }")
                .append(".booking-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }")
                .append(".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }")
                .append(".detail-row:last-child { border-bottom: none; }")
                .append(".detail-label { font-weight: 600; color:rgb(95, 49, 96); }")
                .append(".detail-value { color: #333; }")
                .append(".message-box { background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #906B3A; }")
                .append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h1>✨ Booking Confirmed!</h1>")
                .append("<p>Thank you for choosing us</p>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>Dear <strong>").append(escapeHtml(booking.getName())).append("</strong>,</p>")
                .append("<p>We're excited to confirm your booking with us! Here are your appointment details:</p>")
                .append("<div class='booking-details'>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Service:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getServiceName())).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Date:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getDate())).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Time:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getTime())).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Phone:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getPhone())).append("</span>")
                .append("</div>")
                .append("</div>");
        
        if (booking.getMessage() != null && !booking.getMessage().trim().isEmpty()) {
            htmlContent.append("<div class='message-box'>")
                    .append("<strong>Your Message:</strong><br>")
                    .append("<p style='margin: 10px 0 0 0;'>").append(escapeHtml(booking.getMessage())).append("</p>")
                    .append("</div>");
        }
        
        htmlContent.append("<p>We look forward to serving you! If you need to make any changes or have questions, please don't hesitate to contact us.</p>")
                .append("<div class='footer'>")
                .append("<p>Best regards,<br>The Salon Team</p>")
                .append("<p>If you have any questions, please contact us at ").append(fromEmail).append("</p>")
                .append("</div>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");
        
        return htmlContent.toString();
    }

    private String buildAdminNotificationTemplate(Booking booking) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color: #dc3545; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 30px; }")
                .append(".booking-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }")
                .append(".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }")
                .append(".detail-row:last-child { border-bottom: none; }")
                .append(".detail-label { font-weight: 600; color: #604A31; }")
                .append(".detail-value { color: #333; }")
                .append(".message-box { background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #906B3A; }")
                .append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h1>🔔 New Booking Request</h1>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>A new booking has been submitted:</p>")
                .append("<div class='booking-details'>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Name:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getName())).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Email:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getEmail())).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Phone:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getPhone())).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Service:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getServiceName())).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Date:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getDate())).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Time:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(booking.getTime())).append("</span>")
                .append("</div>")
                .append("</div>");
        
        if (booking.getMessage() != null && !booking.getMessage().trim().isEmpty()) {
            htmlContent.append("<div class='message-box'>")
                    .append("<strong>Customer Message:</strong><br>")
                    .append("<p style='margin: 10px 0 0 0;'>").append(escapeHtml(booking.getMessage())).append("</p>")
                    .append("</div>");
        }
        
        htmlContent.append("<div class='footer'>")
                .append("<p>Please review this booking in the admin panel.</p>")
                .append("</div>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");
        
        return htmlContent.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public void sendPasswordResetEmail(String resetToken, String username) {
        // Only send to lumiereluxe0030@gmail.com
        String adminEmail = "lumiereluxe0030@gmail.com";
        
        try {
            System.out.println("=== Attempting to send password reset email ===");
            System.out.println("From: " + fromEmail);
            System.out.println("To: " + adminEmail);
            System.out.println("Username: " + username);
            System.out.println("Reset Token: " + resetToken);
            System.out.println("Frontend URL: " + frontendUrl);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("Password Reset Request - Admin Account");

            String resetUrl = frontendUrl + "/admin/login?token=" + resetToken;
            System.out.println("Reset URL: " + resetUrl);
            
            String htmlContent = buildPasswordResetTemplate(username, resetUrl);
            helper.setText(htmlContent, true);

            System.out.println("Sending email...");
            mailSender.send(message);
            System.out.println("✅ Password reset email sent successfully to " + adminEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send password reset email due to messaging error");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error details: " + e.getMessage());
            
            // Check if it's an authentication error
            if (e.getMessage() != null && (e.getMessage().contains("authentication") || 
                e.getMessage().contains("535") || e.getMessage().contains("534"))) {
                System.err.println("⚠️ This appears to be an authentication error.");
                System.err.println("Please check:");
                System.err.println("  1. MAIL_USERNAME in .env file (should be your full email address)");
                System.err.println("  2. MAIL_PASSWORD in .env file (should be an App Password, not your regular password)");
                System.err.println("  3. For Gmail: Make sure 'Less secure app access' is enabled OR use an App Password");
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email to " + adminEmail);
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }

    private String buildPasswordResetTemplate(String username, String resetUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #906B3A; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 30px; }" +
                ".button { display: inline-block; padding: 14px 28px; background-color: #906B3A; color: white; text-decoration: none; border-radius: 8px; margin: 20px 0; font-weight: 600; }" +
                ".button:hover { background-color: #7a5a2f; }" +
                ".warning { background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #906B3A; }" +
                ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>🔐 Password Reset Request</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello <strong>" + escapeHtml(username) + "</strong>,</p>" +
                "<p>We received a request to reset your admin account password. Click the button below to reset your password:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetUrl + "' class='button'>Reset Password</a>" +
                "</div>" +
                "<div class='warning'>" +
                "<p><strong>⚠️ Important:</strong></p>" +
                "<ul style='margin: 10px 0; padding-left: 20px;'>" +
                "<li>This link will expire in 1 hour</li>" +
                "<li>If you didn't request this, please ignore this email</li>" +
                "<li>For security, do not share this link with anyone</li>" +
                "</ul>" +
                "</div>" +
                "<p>If the button doesn't work, copy and paste this link into your browser:</p>" +
                "<p style='word-break: break-all; color: #906B3A;'>" + resetUrl + "</p>" +
                "<div class='footer'>" +
                "<p>Best regards,<br>The Admin Team</p>" +
                "<p>This is an automated email. Please do not reply.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public void sendContactFormEmail(String name, String email, String phone, String subject, String message) throws MessagingException {
        try {
            MimeMessage messageObj = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(messageObj, true, "UTF-8");

            // Set sender as the customer
            String senderName = name != null && !name.trim().isEmpty() ? name : "Contact Form User";
            helper.setFrom(fromEmail, senderName);
            
            // Set reply-to as customer's email
            helper.setReplyTo(email);
            
            // Send to contact email
            helper.setTo(contactEmail);
            helper.setSubject("New Contact Form Submission: " + (subject != null && !subject.trim().isEmpty() ? subject : "General Inquiry"));

            String htmlContent = buildContactFormTemplate(name, email, phone, subject, message);
            helper.setText(htmlContent, true);

            mailSender.send(messageObj);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Encoding error: " + e.getMessage(), e);
        }
    }

    private String buildContactFormTemplate(String name, String email, String phone, String subject, String message) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { background-color: #61338A; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }")
                .append(".content { background-color: #f9f9f9; padding: 30px; }")
                .append(".contact-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }")
                .append(".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }")
                .append(".detail-row:last-child { border-bottom: none; }")
                .append(".detail-label { font-weight: 600; color: #61338A; }")
                .append(".detail-value { color: #333; }")
                .append(".message-box { background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #61338A; }")
                .append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h1>📧 New Contact Form Submission</h1>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<p>A new contact form submission has been received:</p>")
                .append("<div class='contact-details'>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Name:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(name != null ? name : "Not provided")).append("</span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Email:</span>")
                .append("<span class='detail-value'><a href='mailto:").append(escapeHtml(email != null ? email : "")).append("'>").append(escapeHtml(email != null ? email : "Not provided")).append("</a></span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Phone:</span>")
                .append("<span class='detail-value'><a href='tel:").append(escapeHtml(phone != null ? phone.replaceAll("[^0-9+]", "") : "")).append("'>").append(escapeHtml(phone != null ? phone : "Not provided")).append("</a></span>")
                .append("</div>")
                .append("<div class='detail-row'>")
                .append("<span class='detail-label'>Subject:</span>")
                .append("<span class='detail-value'>").append(escapeHtml(subject != null && !subject.trim().isEmpty() ? subject : "General Inquiry")).append("</span>")
                .append("</div>")
                .append("</div>");
        
        if (message != null && !message.trim().isEmpty()) {
            htmlContent.append("<div class='message-box'>")
                    .append("<strong>Message:</strong><br>")
                    .append("<p style='margin: 10px 0 0 0; white-space: pre-wrap;'>").append(escapeHtml(message)).append("</p>")
                    .append("</div>");
        }
        
        htmlContent.append("<div class='footer'>")
                .append("<p>This email was sent from the contact form on the website.</p>")
                .append("<p>You can reply directly to this email to respond to the customer.</p>")
                .append("</div>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");
        
        return htmlContent.toString();
    }
}

