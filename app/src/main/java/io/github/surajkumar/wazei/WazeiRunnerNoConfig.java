package io.github.surajkumar.wazei;

import io.github.surajkumar.wazei.noconfig.NoConfigRequestHandler;

public class WazeiRunnerNoConfig {
    private static final String HOST = System.getenv().getOrDefault("host", "localhost");
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("port", "8080"));

    /**
     * Runs the Wazei HTTP server with default or configurable settings.
     *
     * @throws Exception If an error occurs while initializing or starting the server.
     */
    public static void main(String[] args) throws Exception {
        Wazei.init(HOST, PORT, new NoConfigRequestHandler());
    }
}
