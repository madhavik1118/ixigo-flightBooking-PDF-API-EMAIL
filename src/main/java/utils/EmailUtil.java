package utils;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

/**
 * Utility class for sending automated email notifications after test execution.
 * Uses Gmail SMTP with App Password for authentication.
 * Attaches the Extent Report HTML file to the email.
 * 
 * Prerequisites:
 *   - Gmail account with 2FA enabled
 *   - App Password generated (Google Account > Security > App Passwords)
 *   - Network must allow outbound SMTP (port 587 or 465)
 * 
 * If SMTP ports are blocked by firewall/network, enable "Less secure apps" or
 * try from a different network (home WiFi, mobile hotspot).
 */
public class EmailUtil {

    private static final String SMTP_HOST = "smtp.gmail.com";

    /**
     * Sends an email with the test execution report attached.
     * Tries port 587 (TLS) first, falls back to port 465 (SSL).
     */
    public static void sendTestReport(String fromEmail, String appPassword,
                                      List<String> toEmails, String reportPath,
                                      int totalTests, int passedTests,
                                      int failedTests, int skippedTests,
                                      List<String> failedScenarios) {

        System.out.println("[EmailUtil] Preparing to send test report email...");
        System.out.println("[EmailUtil] From: " + fromEmail);
        System.out.println("[EmailUtil] To: " + toEmails);

        // Try TLS (port 587) first
        boolean sent = trySendEmail(fromEmail, appPassword, toEmails, reportPath,
                totalTests, passedTests, failedTests, skippedTests,
                failedScenarios, 587, true);

        // If TLS failed, try SSL (port 465)
        if (!sent) {
            System.out.println("[EmailUtil] Port 587 failed. Trying port 465 (SSL)...");
            sent = trySendEmail(fromEmail, appPassword, toEmails, reportPath,
                    totalTests, passedTests, failedTests, skippedTests,
                    failedScenarios, 465, false);
        }

        if (!sent) {
            System.out.println("========================================");
            System.out.println("  EMAIL COULD NOT BE SENT");
            System.out.println("  Possible reasons:");
            System.out.println("  - SMTP ports (587/465) blocked by firewall");
            System.out.println("  - Network proxy blocking outbound SMTP");
            System.out.println("  - Invalid credentials");
            System.out.println("  Try: Mobile hotspot or home WiFi");
            System.out.println("========================================");
        }
    }

    /**
     * Attempts to send email on a specific port with given protocol settings.
     */
    private static boolean trySendEmail(String fromEmail, String appPassword,
                                        List<String> toEmails, String reportPath,
                                        int totalTests, int passedTests,
                                        int failedTests, int skippedTests,
                                        List<String> failedScenarios,
                                        int port, boolean useTls) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");

        if (useTls) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        } else {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));

            for (String toEmail : toEmails) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            }

            // Email subject with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
            message.setSubject("Test Execution Report - " + timestamp);

            // Create multipart message
            Multipart multipart = new MimeMultipart();

            // Email body - HTML content with test summary
            MimeBodyPart textPart = new MimeBodyPart();
            String emailBody = buildEmailBody(totalTests, passedTests, failedTests,
                    skippedTests, failedScenarios, timestamp);
            textPart.setContent(emailBody, "text/html; charset=utf-8");
            multipart.addBodyPart(textPart);

            // Attach Extent Report
            File reportFile = new File(reportPath);
            if (reportFile.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(reportFile);
                attachmentPart.setFileName("ExtentReport.html");
                multipart.addBodyPart(attachmentPart);
                System.out.println("[EmailUtil] Report attached: " + reportFile.getName()
                        + " (" + reportFile.length() / 1024 + " KB)");
            } else {
                System.out.println("[EmailUtil] WARNING: Report not found at: " + reportPath);
            }

            message.setContent(multipart);

            // Send the email
            System.out.println("[EmailUtil] Connecting to " + SMTP_HOST + ":" + port + "...");
            Transport.send(message);

            System.out.println("========================================");
            System.out.println("  EMAIL SENT SUCCESSFULLY!");
            System.out.println("  From    : " + fromEmail);
            System.out.println("  To      : " + toEmails);
            System.out.println("  Subject : Test Execution Report - " + timestamp);
            System.out.println("  Port    : " + port + (useTls ? " (TLS)" : " (SSL)"));
            System.out.println("========================================");
            return true;

        } catch (Exception e) {
            System.err.println("[EmailUtil] Failed on port " + port + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Builds the HTML email body with test execution summary.
     */
    private static String buildEmailBody(int total, int passed, int failed,
                                         int skipped, List<String> failedScenarios,
                                         String timestamp) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif;'>");
        sb.append("<h2 style='color: #2c3e50;'>&#128202; Automation Test Execution Report</h2>");
        sb.append("<p><strong>Execution Time:</strong> ").append(timestamp).append("</p>");
        sb.append("<p><strong>Project:</strong> ixigo-flightBooking (Selenium + API Automation)</p>");
        sb.append("<hr/>");

        // Summary Table
        sb.append("<h3 style='color: #34495e;'>Test Execution Summary</h3>");
        sb.append("<table border='1' cellpadding='10' cellspacing='0' ");
        sb.append("style='border-collapse: collapse; width: 50%; font-size: 14px;'>");
        sb.append("<tr style='background-color: #3498db; color: white;'>");
        sb.append("<th>Metric</th><th>Count</th></tr>");
        sb.append("<tr><td><strong>Total Tests</strong></td><td><strong>")
                .append(total).append("</strong></td></tr>");
        sb.append("<tr style='background-color: #d4efdf;'><td>&#9989; Passed</td>")
                .append("<td><strong style='color: green;'>").append(passed).append("</strong></td></tr>");
        sb.append("<tr style='background-color: #fadbd8;'><td>&#10060; Failed</td>")
                .append("<td><strong style='color: red;'>").append(failed).append("</strong></td></tr>");
        sb.append("<tr style='background-color: #fdebd0;'><td>&#9888; Skipped</td>")
                .append("<td><strong style='color: orange;'>").append(skipped).append("</strong></td></tr>");
        sb.append("</table>");

        // Pass Rate
        if (total > 0) {
            double passRate = (double) passed / total * 100;
            sb.append("<p><strong>Pass Rate:</strong> ")
                    .append(String.format("%.1f", passRate)).append("%</p>");
        }

        // Failed Scenarios List
        if (failedScenarios != null && !failedScenarios.isEmpty()) {
            sb.append("<h3 style='color: #e74c3c;'>&#10060; Failed Scenarios</h3>");
            sb.append("<ol>");
            for (String scenario : failedScenarios) {
                sb.append("<li style='color: #c0392b; margin: 5px 0;'>").append(scenario).append("</li>");
            }
            sb.append("</ol>");
            sb.append("<p style='color: #e67e22;'><em>Failed scenarios have been re-executed automatically.</em></p>");
        } else {
            sb.append("<p style='color: green; font-size: 16px;'>");
            sb.append("<strong>&#127881; All scenarios passed successfully!</strong></p>");
        }

        sb.append("<hr/>");
        sb.append("<p><em>&#128206; The detailed Extent Report is attached to this email.</em></p>");
        sb.append("<br/>");
        sb.append("<p style='color: #7f8c8d; font-size: 11px;'>");
        sb.append("This is an automated email from the ixigo-flightBooking Test Automation Framework.<br/>");
        sb.append("Do not reply to this email.</p>");
        sb.append("</body></html>");

        return sb.toString();
    }

    /**
     * Sends email using hardcoded config values.
     * Called after test execution from PostExecutionHandler or MasterTestRunner.
     */
    public static void sendReportFromConfig(String reportPath, int total, int passed,
                                            int failed, int skipped,
                                            List<String> failedScenarios) {
        String fromEmail = "kmadhavi1811@gmail.com";
        String appPassword = "qxyh hqyw fkeq pvpw";
        List<String> toEmails = List.of("kmadhavi1811@gmail.com");

        sendTestReport(fromEmail, appPassword, toEmails, reportPath,
                total, passed, failed, skipped, failedScenarios);
    }
}
