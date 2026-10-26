package com.example.nulsf.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/nulsf}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUser;

    @Value("${spring.datasource.password:admin}")
    private String mysqlPass;

    @Bean
    @Primary
    public DataSource dataSource() {
        boolean mysqlAvailable = isMySqlAccessible(mysqlUrl, mysqlUser, mysqlPass);

        if (mysqlAvailable) {
            System.out.println("==================================================================");
            System.out.println("[NU-LSF] Successfully connected to MySQL database: " + mysqlUrl);
            System.out.println("==================================================================");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(mysqlUrl);
            config.setUsername(mysqlUser);
            config.setPassword(mysqlPass);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(10);
            return new HikariDataSource(config);
        } else {
            System.out.println("==================================================================");
            System.out.println("[NU-LSF NOTICE] MySQL Server is not running or not reachable.");
            System.out.println("[NU-LSF NOTICE] Auto-activating Embedded H2 Database (MySQL mode).");
            System.out.println("[NU-LSF NOTICE] Storing data safely at: ./data/nulsf.mv.db");
            System.out.println("[NU-LSF NOTICE] Application will run 100% functionally offline!");
            System.out.println("==================================================================");

            // Ensure ./data directory exists
            new File("./data").mkdirs();

            HikariConfig h2Config = new HikariConfig();
            h2Config.setJdbcUrl("jdbc:h2:file:./data/nulsf;MODE=MySQL;AUTO_SERVER=TRUE");
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setUsername("sa");
            h2Config.setPassword("");
            h2Config.setMaximumPoolSize(10);
            return new HikariDataSource(h2Config);
        }
    }

    private boolean isMySqlAccessible(String url, String user, String pass) {
        // Quick socket probe first so we don't hang if MySQL host/port is down
        try {
            String host = "localhost";
            int port = 3306;

            if (url != null && url.startsWith("jdbc:mysql://")) {
                String clean = url.substring("jdbc:mysql://".length());
                int slash = clean.indexOf('/');
                if (slash != -1) {
                    clean = clean.substring(0, slash);
                }
                int colon = clean.indexOf(':');
                if (colon != -1) {
                    host = clean.substring(0, colon);
                    port = Integer.parseInt(clean.substring(colon + 1));
                } else {
                    host = clean;
                }
            }

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 800);
            }

            // If socket is open, verify JDBC connection
            DriverManager.setLoginTimeout(2);
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
