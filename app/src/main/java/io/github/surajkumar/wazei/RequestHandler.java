package io.github.surajkumar.wazei;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Handles incoming HTTP requests and delegates processing to a {@link RequestProcessor}.
 *
 * @author Suraj Kumar
 */
public class RequestHandler implements HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ConfigSearcher configSearcher;

    /**
     * Constructs a RequestHandler with a given ConfigSearcher.
     *
     * @param configSearcher The ConfigSearcher to use for retrieving configurations.
     */
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

    /**
     * Processes an incoming HTTP request.
     *
     * @param exchange The HttpExchange object representing the incoming request and response.
     * @throws IOException If an IO error occurs during request processing.
     */
    private void processRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String remoteAddress = exchange.getRemoteAddress().getHostString();

        LOGGER.info("Received request from {} for path: {}", remoteAddress, path);

        try {
            Config config = configSearcher.getConfigForPath(path);
            if (config == null) {
                sendResponse(
                        exchange,
                        HTTPStatusCode.NOT_FOUND.getCode(),
                        new HashMap<>(),
                        "Page Not Found");
                return;
            }

            String method = exchange.getRequestMethod();
            Map<String, String> queryParameters =
                    QueryStringParser.getParametersFromQueryString(exchange.getRequestURI());
            Headers headers = exchange.getRequestHeaders();
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
                sendResponse(
                        exchange,
                        HTTPStatusCode.BAD_REQUEST.getCode(),
                        new HashMap<>(),
                        "Bad request");
            }

        } catch (Exception e) {
            sendResponse(
                    exchange,
                    HTTPStatusCode.INTERNAL_SERVER_ERROR.getCode(),
                    new HashMap<>(),
                    "Internal Server Error");
            LOGGER.error("Error processing request", e);
        }
    }

    /**
     * Sends a JSON response to the client.
     *
     * @param exchange The HttpExchange object representing the response.
     * @param statusCode The HTTP status code for the response.
     * @param responseObject The object to be serialized to JSON and sent as the response body.
     * @throws IOException If an IO error occurs during response handling.
     */
    public static void sendJsonResponse(
            HttpExchange exchange, HTTPStatusCode statusCode, Object responseObject)
            throws IOException {

        Map<String, String> headers = new HashMap<>();

        String jsonResponse = OBJECT_MAPPER.writeValueAsString(responseObject);

        /* Parse for any headers or response codes */
        JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonResponse);

        if (jsonNode.has("httpHeaders") && jsonNode.get("httpHeaders").isObject()) {
            JsonNode headersNode = jsonNode.get("httpHeaders");
            Iterator<Map.Entry<String, JsonNode>> fields = headersNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                headers.put(key, value.asText());
            }
            ((ObjectNode) jsonNode).remove("httpHeaders");
        }

        int status = statusCode.getCode();

        if (jsonNode.has("httpStatusCode")) {
            try {
                JsonNode statusNode = jsonNode.get("httpStatusCode");
                status = statusNode.intValue();
                ((ObjectNode) jsonNode).remove("httpStatusCode");
            } catch (Exception ignore) {
            }
        }

        sendResponse(exchange, status, headers, jsonNode.toPrettyString());
    }

    /**
     * Sends an empty response to the client.
     *
     * @param exchange The HttpExchange object representing the response.
     * @param statusCode The HTTP status code for the response.
     * @throws IOException If an IO error occurs during response handling.
     */
    private void sendEmptyResponse(HttpExchange exchange, HTTPStatusCode statusCode)
            throws IOException {
        sendResponse(exchange, statusCode.getCode(), new HashMap<>(), "");
    }

    /**
     * Handles IO errors during request processing.
     *
     * @param exchange The HttpExchange object representing the response.
     * @param e The IOException that occurred.
     */
    private void handleError(HttpExchange exchange, IOException e) {
        try {
            sendResponse(
                    exchange,
                    HTTPStatusCode.INTERNAL_SERVER_ERROR.getCode(),
                    new HashMap<>(),
                    "Internal Server Error");
            LOGGER.error("IO error while processing request", e);
        } catch (IOException ioException) {
            LOGGER.error("Error sending error response", ioException);
        }
    }

    /**
     * Sends an HTTP response to the client.
     *
     * @param exchange The HttpExchange object representing the response.
     * @param statusCode The HTTP status code for the response.
     * @param response The response body to be sent to the client.
     * @throws IOException If an IO error occurs during response handling.
     */
    private static void sendResponse(
            HttpExchange exchange, int statusCode, Map<String, String> headers, String response)
            throws IOException {

        headers.forEach((k, v) -> exchange.getResponseHeaders().add(k, v));
        exchange.sendResponseHeaders(statusCode, response.length());

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
