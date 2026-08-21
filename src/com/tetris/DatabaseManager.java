package com.tetris;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    // IMPORTANT: Update this URL to point to your online API
    // For local testing, keep it as localhost:3000
    private static final String API_URL = "https://tetris-dungeon.onrender.com/api/scores";
    
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static class HighScore {
        public String name;
        public int score;
        public int level;

        public HighScore(String name, int score, int level) {
            this.name = name;
            this.score = score;
            this.level = level;
        }
    }

    public static void initializeDatabase() {
        System.out.println("No longer using direct JDBC. Database initialization is now handled by the Backend API.");
    }

    public static void saveHighScore(String name, int score, int level) {
        try {
            String jsonBody = String.format("{\"name\": \"%s\", \"score\": %d, \"level\": %d}", 
                                            name.replace("\"", "\\\""), score, level);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("API Error: Failed to save score.");
            }
        } catch (Exception e) {
            System.err.println("Error saving high score over HTTP: " + e.getMessage());
        }
    }

    public static List<HighScore> getTopScores(int limit) {
        List<HighScore> scores = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String[] lines = response.body().split("\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        scores.add(new HighScore(
                            parts[0], 
                            Integer.parseInt(parts[1]), 
                            Integer.parseInt(parts[2])
                        ));
                    }
                }
            } else {
                System.err.println("API Error: Failed to fetch scores.");
            }
        } catch (Exception e) {
            System.err.println("Error fetching high scores over HTTP: " + e.getMessage());
            // Add a placeholder if the server is offline
            scores.add(new HighScore("Server Offline", 0, 0));
        }
        
        return scores;
    }
}
