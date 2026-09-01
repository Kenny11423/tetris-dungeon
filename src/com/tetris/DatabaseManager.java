package com.tetris;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    // MySQL Connection configuration
    private static final String URL = "jdbc:mysql://localhost:3306/tetris_dungeon?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Fallback list when MySQL server is offline during standalone execution
    private static final List<HighScore> fallbackScores = new ArrayList<>();

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

    private static Connection getConnection() throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Fallback for older MySQL drivers if present
            Class.forName("com.mysql.jdbc.Driver");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS high_scores (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "name VARCHAR(50) NOT NULL, " +
                         "score INT NOT NULL, " +
                         "level INT NOT NULL, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                         ")";
            stmt.execute(sql);
            System.out.println("[JDBC-MySQL] Database and table initialized successfully.");
        } catch (Exception e) {
            System.out.println("[JDBC-MySQL Notice] Local MySQL server not detected (" + e.getMessage() + "). Memory fallback active.");
        }
    }

    public static void saveHighScore(String name, int score, int level) {
        String sql = "INSERT INTO high_scores (name, score, level) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            pstmt.setInt(3, level);
            pstmt.executeUpdate();
            System.out.println("[JDBC-MySQL] Saved score successfully for: " + name);
        } catch (Exception e) {
            System.out.println("[JDBC-MySQL Notice] Saved score locally: " + name);
            fallbackScores.add(new HighScore(name, score, level));
        }
    }

    public static List<HighScore> getTopScores(int limit) {
        List<HighScore> scores = new ArrayList<>();
        String sql = "SELECT name, score, level FROM high_scores ORDER BY score DESC, level DESC LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                scores.add(new HighScore(
                    rs.getString("name"),
                    rs.getInt("score"),
                    rs.getInt("level")
                ));
            }
        } catch (Exception e) {
            fallbackScores.sort((a, b) -> Integer.compare(b.score, a.score));
            for (int i = 0; i < Math.min(limit, fallbackScores.size()); i++) {
                scores.add(fallbackScores.get(i));
            }
        }
        
        return scores;
    }
}
