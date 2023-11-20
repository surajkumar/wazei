package io.github.surajkumar.wazei.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.surajkumar.wazei.bootstrap.config.Config;
import io.github.surajkumar.wazei.bootstrap.config.ConfigSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;


public class RequestHandler implements HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
    private final ConfigSearcher configSearcher;

    public RequestHandler(ConfigSearcher configSearcher) {
        this.configSearcher = configSearcher;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        LOGGER.info("Received request [{}] {}", exchange.getRemoteAddress().getAddress().getHostAddress(), path);
        try {
            Config config = configSearcher.getConfigForPath(path);
            if (config == null) {
                sendResponse(exchange, HTTPStatusCode.NOT_FOUND, "Page Not Found");
                return;
            }
            RequestProcessor processor = new RequestProcessor(config);
            processor.processRequest(exchange, path);
        } catch (Exception e) {
            sendResponse(exchange, HTTPStatusCode.INTERNAL_SERVER_ERROR, "Internal Server Error");
            e.printStackTrace();
        }
    }

    private static void sendResponse(HttpExchange exchange, HTTPStatusCode statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode.getCode(), response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
