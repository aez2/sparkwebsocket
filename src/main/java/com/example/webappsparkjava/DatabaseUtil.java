package com.example.webappsparkjava;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {

    public static Connection getConnection() throws URISyntaxException, SQLException {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null) {
            // Fallback connection string for local development (adjust accordingly)
            databaseUrl = "postgres://uabq2j1087ocl4:p04b0acd3f412693f9ddd1f99f64612a4d340c9f0ce15602a186a94ec9262c9bc@cd27da2sn4hj7h.cluster-czrs8kj4isg7.us-east-1.rds.amazonaws.com:5432/d2n54f83p95vhh";
            System.out.println("DATABASE_URL not set, using fallback: " + databaseUrl);
        }
        URI dbUri = new URI(databaseUrl);

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("sslmode", "require");

        return DriverManager.getConnection(dbUrl, props);
    }
}
