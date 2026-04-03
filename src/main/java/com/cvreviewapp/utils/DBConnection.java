package com.cvreviewapp.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced DBConnection using HikariCP for production-ready connection pooling.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                LOGGER.severe("Unable to find application.properties");
                throw new RuntimeException("application.properties not found");
            }
            props.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size", "10")));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);
            LOGGER.info("HikariCP connection pool initialized successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize HikariCP connection pool", e);
            throw new RuntimeException("Fail to initialize HikariCP connection pool", e);
        }
    }

    private DBConnection() {}

    /**
     * @return Connection from the HikariCP pool
     * @throws SQLException If a connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Closes the entire data source — call this upon application exit.
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            LOGGER.info("HikariCP connection pool shut down successfully.");
        }
    }
}

/**
 * AUTH_WATERMARK_START
 * Verified Owner: azihad
 * Contact: azihad783@gmail.com
 * Github: AZtheE1
 * AUTH_WATERMARK_END
 */