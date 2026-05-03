package Util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);

    public static void initializeDatabase() {
        try {
            if (!isInitialized()) {
                logger.info("Database not initialized. Running initialization script...");
                executeSqlScript();
                logger.info("Database initialized successfully");
            } else {
                logger.info("Database already initialized");
            }
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static boolean isInitialized() throws SQLException {
        try (ConnectionHolder connHolder = DatabaseConnection.getConnection();
             Statement stmt = connHolder.get().createStatement()) {

            String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='offices'";
            ResultSet rs = stmt.executeQuery(query);
            return rs.next();
        }
    }

    private static void executeSqlScript() throws SQLException {
        try (InputStream scriptStream = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("DatabaseInit.sql")) {

            if (scriptStream == null) {
                throw new RuntimeException("DatabaseInit.sql not found in resources");
            }

            String sqlScript = new BufferedReader(new InputStreamReader(scriptStream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            try (ConnectionHolder connHolder = DatabaseConnection.getConnection();
                 Statement stmt = connHolder.get().createStatement()) {
                String[] statements = sqlScript.split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        logger.debug("Executing: {}", trimmed.substring(0, Math.min(50, trimmed.length())) + "...");
                        stmt.execute(trimmed);
                    }
                }
                logger.info("Database initialization script executed successfully");
            }
        } catch (Exception e) {
            logger.error("Error executing database initialization script", e);
            throw new RuntimeException("Failed to execute database initialization script", e);
        }
    }
}
