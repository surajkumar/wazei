package io.github.surajkumar.wazei;

import io.github.surajkumar.wazei.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wazei {
    private static final Logger LOGGER = LoggerFactory.getLogger(Wazei.class);

    public static void init(String host, int port) throws Exception {
        HTTPServer webServer = new HTTPServer(ConfigLoader.loadConfig());
        webServer.create(host, port);
        LOGGER.info("Server started on http://{}:{}", host, port);
    }
}
