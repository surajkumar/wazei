package io.github.surajkumar.wazei;

/**
 * A simple runner class for starting the Wazei HTTP server with default or
 * configurable settings.
 *
 * This class initializes and starts the Wazei HTTP server using the default
 * host and port values or those provided through environment variables. It
 * serves as a quick setup for those who do not need to customize the server
 * configuration.
 *
 * The host and port values are read from the "host" and "port" environment
 * variables. If these variables are not set, the default values are used
 * (localhost and 8080, respectively).
 *
 * This class is specifically to be used with the `wazei:run` gradle action
 *
 * @Author Suraj Kumar
 */
public class WazeiRunner {
    private static final String HOST = System.getenv().getOrDefault("host", "localhost");
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("port", "8080"));

    /**
     * Runs the Wazei HTTP server with default or configurable settings.
     *
     * @throws Exception
     *             If an error occurs while initializing or starting the server.
     */
    public static void main(String[] args) throws Exception {
        Wazei.init(HOST, PORT);
    }
}
