package com.chat.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads server configuration from .properties file.
 * Supports flexible deployment without code recompilation.
 */
public class ConfigLoader {
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_PORT = 3000;

    private final String host;
    private final int port;

    public ConfigLoader() throws IOException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            }
        }
        this.host = props.getProperty("server.host", DEFAULT_HOST);
        String portStr = props.getProperty("server.port", String.valueOf(DEFAULT_PORT));
        this.port = Integer.parseInt(portStr.trim());
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
