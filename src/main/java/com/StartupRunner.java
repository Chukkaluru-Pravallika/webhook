package com.test.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String webhookUrl;
    private String accessToken;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=================================");
        System.out.println("🚀 API TEST STARTED");
        System.out.println("=================================\n");
        
        // Generate webhook
        boolean success = generateWebhook();
        
        if (success) {
            // Hardcoded values - no input needed!
            String regNo = "REG12347";
            String sqlQuery = "SELECT \n" +
                "    d.DEPARTMENT_NAME,\n" +
                "    MAX(p.AMOUNT) as SALARY,\n" +
                "    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) as EMPLOYEE_NAME,\n" +
                "    EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.DOB)) as AGE\n" +
                "FROM DEPARTMENT d\n" +
                "JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT\n" +
                "JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID\n" +
                "WHERE EXTRACT(DAY FROM p.PAYMENT_TIME) != 1\n" +
                "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME, e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB\n" +
                "HAVING p.AMOUNT = (\n" +
                "    SELECT MAX(p2.AMOUNT)\n" +
                "    FROM PAYMENTS p2\n" +
                "    JOIN EMPLOYEE e2 ON p2.EMP_ID = e2.EMP_ID\n" +
                "    WHERE e2.DEPARTMENT = e.DEPARTMENT\n" +
                "    AND EXTRACT(DAY FROM p2.PAYMENT_TIME) != 1\n" +
                ")\n" +
                "ORDER BY d.DEPARTMENT_NAME;";
            
            String lastTwoDigits = regNo.substring(regNo.length() - 2);
            int lastTwoNum = Integer.parseInt(lastTwoDigits);
            
            System.out.println("\n📋 Registration Number: " + regNo);
            System.out.println("🔢 Last two digits: " + lastTwoDigits);
            
            if (lastTwoNum % 2 != 0) {
                System.out.println("📌 Solving: QUESTION 1 (Odd number)");
            } else {
                System.out.println("📌 Solving: QUESTION 2 (Even number)");
            }
            
            System.out.println("\n📝 SQL Query being submitted:");
            System.out.println("----------------------------------------");
            System.out.println(sqlQuery);
            System.out.println("----------------------------------------");
            
            submitSolution(sqlQuery);
        }
    }
    
    private boolean generateWebhook() {
        try {
            String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            
            // REPLACE WITH YOUR ACTUAL DETAILS
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", "John Doe");      // Your full name
            requestBody.put("regNo", "REG12347");     // Your registration number
            requestBody.put("email", "john@example.com"); // Your email
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            System.out.println("📡 Generating webhook...");
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );
            
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            webhookUrl = jsonResponse.get("webhook").asText();
            accessToken = jsonResponse.get("accessToken").asText();
            
            System.out.println("✅ Webhook generated successfully!");
            System.out.println("🔗 Webhook URL: " + webhookUrl);
            System.out.println("🔑 Access Token: " + accessToken);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error generating webhook: " + e.getMessage());
            return false;
        }
    }
    
    private void submitSolution(String sqlQuery) {
        try {
            if (webhookUrl == null || accessToken == null) {
                System.err.println("❌ Error: Webhook or Access Token not available!");
                return;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("query", sqlQuery);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            System.out.println("\n📡 Submitting your SQL solution...");
            ResponseEntity<String> response = restTemplate.exchange(
                webhookUrl, HttpMethod.POST, entity, String.class
            );
            
            System.out.println("✅ Submission successful!");
            System.out.println("📨 Response: " + response.getBody());
            System.out.println("📊 Status Code: " + response.getStatusCode());
            
        } catch (Exception e) {
            System.err.println("❌ Error submitting solution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}