package com.example.nulsf.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiService {
    private static final String BASE_URL = "http://localhost:8080/api";
    public static String authToken = null;

    public boolean sendAuthRequest(String email, String password, String username, String contact, boolean isRegister) throws Exception {
        String path = isRegister ? "/auth/register" : "/auth/login";
        URL url = URI.create(BASE_URL + path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Build JSON manually or use a library like Gson/Jackson
        String jsonInput;
        if (isRegister) {
            jsonInput = String.format("{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", username, email, password);
        } else {
            jsonInput = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code == 200 || code == 201) {
            if (!isRegister) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    authToken = br.readLine(); // Store JWT token
                }
            }
            return true;
        }
        return false;
    }
}