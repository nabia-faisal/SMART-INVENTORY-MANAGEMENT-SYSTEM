package dao;

import config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Provides a MySQL database connection using credentials from AppConfig.
 */
public class ConnectionProvider {

    public static Connection getCon() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                AppConfig.DB_URL,
                AppConfig.DB_USER,
                AppConfig.DB_PASSWORD
            );
        } catch (Exception e) {
            System.out.println("Error in ConnectionProvider: " + e);
            return null;
        }
    }
}
