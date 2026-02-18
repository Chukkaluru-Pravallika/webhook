package com.test.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
        
        // Try to generate webhook
        boolean success = generateWebhook();
        
        if (success) {
            // Get registration number from user
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nEnter your registration number (e.g., REG12347): ");
            String regNo = scanner.nextLine().trim();
            
            String lastTwoDigits = regNo.substring(regNo.length() - 2);
            int lastTwoNum = Integer.parseInt(lastTwoDigits);
            
            System.out.println("\n📋 Your Registration Number: " + regNo);
            System.out.println("🔢 Last two digits: " + lastTwoDigits);
            
            String questionLink;
            if (lastTwoNum % 2 != 0) {
                questionLink = "https://drive.google.com/file/d/1LAPx2to9zmN5NDY0tkMrJRnVXf_1guNr/view";
                System.out.println("📌 You need to solve: QUESTION 1 (Odd number)");
            } else {
                questionLink = "https://drive.google.com/file/d/1b0p5C-6fUrUQglJVaWWAAB3P12IfoBCH/view";
                System.out.println("📌 You need to solve: QUESTION 2 (Even number)");
            }
            
            System.out.println("🔗 Google Drive Link: " + questionLink);
            
            // Get SQL solution from user
            System.out.print("\n📝 Paste your SQL query: ");
            String sqlQuery = scanner.nextLine();
            
            submitSolution(sqlQuery);
            scanner.close();
        } else {
            System.out.println("\n❌ Failed to generate webhook. Please check:");
            System.out.println("1. Your internet connection");
            System.out.println("2. If the API endpoint is correct");
            System.out.println("3. Try running the application again");
        }
    }
    
    private boolean generateWebhook() {
        try {
            String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            
            // REPLACE THESE WITH YOUR ACTUAL DETAILS
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
            
        } catch (ResourceAccessException e) {
            System.err.println("❌ Network error: Cannot connect to the API server");
            System.err.println("   Please check your internet connection");
            return false;
        } catch (RestClientException e) {
            System.err.println("❌ API error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
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