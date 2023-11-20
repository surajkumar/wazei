package io.github.surajkumar.wazei;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.github.surajkumar.wazei.config.Config;
import io.github.surajkumar.wazei.config.ConfigSearcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class RequestHandler implements HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ConfigSearcher configSearcher;

    public RequestHandler(ConfigSearcher configSearcher) {
        this.configSearcher = configSearcher;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            processRequest(exchange);
        } catch (IOException e) {
            handleError(exchange, e);
        }
    }

    private void processRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String remoteAddress = exchange.getRemoteAddress().getHostString();

        LOGGER.info("Received request from {} for path: {}", remoteAddress, path);

        try {
            Config config = configSearcher.getConfigForPath(path);
            if (config == null) {
                sendResponse(exchange, HTTPStatusCode.NOT_FOUND, "Page Not Found");
                return;
            }

            String method = exchange.getRequestMethod();
            Map<String, String> queryParameters =
                    QueryStringParser.getParametersFromQueryString(exchange.getRequestURI());
            Headers headers = exchange.getResponseHeaders();
            InputStream body = exchange.getRequestBody();

            RequestProcessor processor = new RequestProcessor(config);
            MethodResponse response =
                    processor.processRequest(path, method, queryParameters, headers, body);

            if (response != null) {
                if (response.containsResponse()) {
                    sendJsonResponse(exchange, HTTPStatusCode.OK, response.getResponse());
                } else {
                    sendEmptyResponse(exchange, HTTPStatusCode.OK);
                }
            } else {
                sendResponse(exchange, HTTPStatusCode.BAD_REQUEST, "Bad request");
            }

        } catch (Exception e) {
            sendResponse(exchange, HTTPStatusCode.INTERNAL_SERVER_ERROR, "Internal Server Error");
            LOGGER.error("Error processing request", e);
        }
    }

    private void sendJsonResponse(
            HttpExchange exchange, HTTPStatusCode statusCode, Object responseObject)
            throws IOException {
        String jsonResponse =
                OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(responseObject);
        sendResponse(exchange, statusCode, jsonResponse);
    }

    private void sendEmptyResponse(HttpExchange exchange, HTTPStatusCode statusCode)
            throws IOException {
        sendResponse(exchange, statusCode, "");
    }

    private void handleError(HttpExchange exchange, IOException e) {
        try {
            sendResponse(exchange, HTTPStatusCode.INTERNAL_SERVER_ERROR, "Internal Server Error");
            LOGGER.error("IO error while processing request", e);
        } catch (IOException ioException) {
            LOGGER.error("Error sending error response", ioException);
        }
    }

    private static void sendResponse(
            HttpExchange exchange, HTTPStatusCode statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode.getCode(), response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
