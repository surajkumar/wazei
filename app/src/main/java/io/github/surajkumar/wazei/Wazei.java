package io.github.surajkumar.wazei;

import com.sun.net.httpserver.HttpHandler;
import io.github.surajkumar.wazei.config.ConfigLoader;

import io.github.surajkumar.wazei.config.ConfigSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wazei {
    private static final Logger LOGGER = LoggerFactory.getLogger(Wazei.class);

    public static void init(String host, int port, HttpHandler handler) throws Exception {
        HTTPServer webServer = new HTTPServer();
        webServer.create(host, port, handler);
        LOGGER.info("Server started on http://{}:{}", host, port);
    }
}
