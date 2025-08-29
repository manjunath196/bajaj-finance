package com.bajajfinserv.health;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class HealthQualifierApplication implements CommandLineRunner {

    private static final String GENERATE_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    // Replace with your details
    private static final String NAME = "John Doe";
    private static final String REG_NO = "REG12347"; // odd → Question 1
    private static final String EMAIL = "john@example.com";

    // Final SQL Query for Question 1 (Odd)
    private static final String FINAL_SQL_QUERY =
            "SELECT c.customer_id, c.customer_name " +
            "FROM Customers c " +
            "LEFT JOIN Orders o ON c.customer_id = o.customer_id " +
            "WHERE o.customer_id IS NULL;";

    public static void main(String[] args) {
        SpringApplication.run(HealthQualifierApplication.class, args);
    }

    @Override
    public void run(String... args) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Step 1: Call generateWebhook API
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", NAME);
            requestBody.put("regNo", REG_NO);
            requestBody.put("email", EMAIL);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    GENERATE_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String webhookUrl = (String) response.getBody().get("webhook");
                String accessToken = (String) response.getBody().get("accessToken");

                System.out.println("Webhook URL: " + webhookUrl);
                System.out.println("Access Token: " + accessToken);

                // Step 2: Post final SQL query to webhook
                HttpHeaders authHeaders = new HttpHeaders();
                authHeaders.setContentType(MediaType.APPLICATION_JSON);
                authHeaders.setBearerAuth(accessToken);

                Map<String, String> finalQueryBody = new HashMap<>();
                finalQueryBody.put("finalQuery", FINAL_SQL_QUERY);

                HttpEntity<Map<String, String>> finalEntity = new HttpEntity<>(finalQueryBody, authHeaders);

                ResponseEntity<String> submitResponse = restTemplate.exchange(
                        webhookUrl,
                        HttpMethod.POST,
                        finalEntity,
                        String.class
                );

                System.out.println("Submit Response: " + submitResponse.getBody());
            } else {
                System.err.println("Failed to get webhook response.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
