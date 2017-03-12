package com.salesforce.webcrawler;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadProperties {
    private final static Logger LOGGER = LoggerFactory.getLogger(LoadProperties.class);
    private static final String DEFAULT_CONFIG_PATH = "properties/env.properties";

    private Properties properties = null;

    public LoadProperties() {
        String propertiesFilePath = DEFAULT_CONFIG_PATH;

        properties = new Properties();
        InputStream in = null;
        try {
            in = LoadProperties.class.getClassLoader().getResourceAsStream(propertiesFilePath);
            LOGGER.info("action=loadProperties, path=\"{}\"", propertiesFilePath);

            properties.load(in);
            LOGGER.info("action=loadProperties Completed");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load configuration file.", ex);
        } finally {
            // releases system resources associated with stream
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    private String getProperty(String propertyKey) { return properties.getProperty(propertyKey); }

    public String getRedisHost() { return getProperty("redis-host"); }

    public int getRedisPort() { return Integer.parseInt(getProperty("redis-port")); }

}
