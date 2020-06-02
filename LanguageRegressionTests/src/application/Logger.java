package application;

/**
 * The logger singleton class.
 * @author Evan Gunn
 *
 */
public class Logger {
    private static Logger logger = null;
    
    /**
     * @return The single instance of logger.
     */
    public static Logger getInstance() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }
    
    /**
     * This method decouples the message from the method of logging.
     * @param message The message to be logged.
     */
    public void log(String message) {
        System.out.println(message);
    }
}
