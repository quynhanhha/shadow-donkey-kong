package game;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for file I/O operations.
 */
public class IOUtils {
    private static final Logger LOGGER = Logger.getLogger(IOUtils.class.getName());
    
    // Private constructor to prevent instantiation
    private IOUtils() {} 

    /**
     * Reads a properties file and returns its contents as a Properties object.
     *
     * @param filePath Path to the properties file
     * @return Properties object containing the file contents
     */
    public static Properties readPropertiesFile(String filePath) {
        Properties props = new Properties();
        
        try (FileInputStream in = new FileInputStream(filePath)) {
            props.load(in);
            LOGGER.fine("Successfully loaded properties from: " + filePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading properties file: " + filePath, e);
        }      
        return props;
    }
} 