package com.cvreviewapp.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();
    static {
        try {
            props.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("[Config] Could not load config.properties: " + e.getMessage());
        }
    }
    public static String get(String key, String defaultValue) {
        String env = System.getenv(key);
        if (env != null) return env;
        return props.getProperty(key, defaultValue);
    }
    public static String get(String key) {
        return get(key, null);
    }
} 