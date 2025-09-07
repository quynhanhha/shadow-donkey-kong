package game.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central configuration manager for the game.
 */
public final class Config {
    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());
    
    // File paths
    private static final String APP_PROPERTIES_PATH = "res/app.properties";
    private static final String MESSAGE_PROPERTIES_PATH = "res/message.properties";
    
    // Default values
    private static final int DEFAULT_INT_VALUE = 0;
    private static final double DEFAULT_DOUBLE_VALUE = 0.0;
    private static final String DEFAULT_STRING_VALUE = "";
    private static final boolean DEFAULT_BOOLEAN_VALUE = false;
    
    // Boolean string representations
    private static final String[] TRUE_STRINGS = {"true", "1", "yes", "y"};
    private static final String[] FALSE_STRINGS = {"false", "0", "no", "n"};
    
    // Properties objects to hold application and message configurations
    private static final Properties appProps = new Properties();
    private static final Properties msgProps = new Properties();
    
    // Prevent instantiation
    private Config() {
        throw new AssertionError("Config utility class should not be instantiated");
    }

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // Load application properties
        try (FileInputStream appIn = new FileInputStream(APP_PROPERTIES_PATH)) {
            appProps.load(appIn);
            LOGGER.info("Successfully loaded application properties");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load application properties file", e);
            throw new RuntimeException("Failed to initialize application configuration", e);
        }
        
        // Load message properties
        try (FileInputStream msgIn = new FileInputStream(MESSAGE_PROPERTIES_PATH)) {
            msgProps.load(msgIn);
            LOGGER.info("Successfully loaded message properties");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load message properties file", e);
            throw new RuntimeException("Failed to initialize message configuration", e);
        }
    }

    /**
     * Retrieves a string property value from app.properties.
     *
     * @param key The property key
     * @return The property value, or empty string if not found
     */
    public static String getApp(String key) {
        String value = appProps.getProperty(key);
        if (value == null) {
            LOGGER.warning(() -> String.format("Missing key in app.properties: %s", key));
            return DEFAULT_STRING_VALUE;
        }
        return value;
    }

    /**
     * Retrieves an integer property value from app.properties.
     *
     * @param key The property key
     * @return The property value as integer, or 0 if not found or invalid
     */
    public static int getAppInt(String key) {
        try {
            return Integer.parseInt(getApp(key));
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> String.format("Value for '%s' is not a valid integer: %s", key, getApp(key)));
            return DEFAULT_INT_VALUE;
        }
    }

    /**
     * Retrieves a double property value from app.properties.
     */
    public static double getAppDouble(String key) {
        try {
            return Double.parseDouble(getApp(key));
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> String.format("Value for '%s' is not a valid double: %s", key, getApp(key)));
            return DEFAULT_DOUBLE_VALUE;
        }
    }

    /**
     * Retrieves a boolean property value from app.properties.
     */
    public static boolean getAppBoolean(String key) {
        String value = getApp(key).toLowerCase();
        
        // Check for true values
        for (String trueStr : TRUE_STRINGS) {
            if (value.equals(trueStr)) {
                return true;
            }
        }
        
        // Check for false values
        for (String falseStr : FALSE_STRINGS) {
            if (value.equals(falseStr)) {
                return false;
            }
        }
        
        // Log warning for values that don't match any pattern
        if (!value.isEmpty()) {
            LOGGER.warning(() -> String.format("Value for '%s' is not a valid boolean: %s", key, value));
        }
        
        return DEFAULT_BOOLEAN_VALUE;
    }

    /**
     * Retrieves a message from message.properties.
     *
     * @param key The message key
     * @return The message value, or empty string if not found
     */
    public static String getMsg(String key) {
        String value = msgProps.getProperty(key);
        if (value == null) {
            LOGGER.warning(() -> String.format("Missing key in message.properties: %s", key));
            return DEFAULT_STRING_VALUE;
        }
        return value;
    }

    /**
     * Reloads all properties from files.
     */
    public static void reload() {
        loadProperties();
        LOGGER.info("Configuration reloaded");
    }
    
    /**
     * Gets the total number of properties loaded
     */
    public static int getPropertyCount() {
        return appProps.size() + msgProps.size();
    }
}
