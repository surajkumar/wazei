package io.github.surajkumar.wazei.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.github.surajkumar.wazei.bootstrap.config.Argument;
import io.github.surajkumar.wazei.bootstrap.config.Config;
import io.github.surajkumar.wazei.bootstrap.config.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestProcessor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Config config;

    public RequestProcessor(Config config) {
        this.config = config;
    }

    public void processRequest(HttpExchange exchange, String path) throws Exception {
        String packageName = config.getPackageName();
        String className = config.getClassName();
        Class<?> controllerClass = Class.forName(packageName + "." + className);
        Object instance = createInstance(controllerClass);
        List<Metadata> metadataList = config.getMetadata();

        for (Metadata metadata : metadataList) {
            handleMetadata(exchange, path, getParametersFromQueryString(exchange.getRequestURI()), metadata, instance);
        }

        sendResponse(exchange, HTTPStatusCode.NOT_FOUND, "Page Not Found");
    }

    private Object createInstance(Class<?> controllerClass) throws ReflectiveOperationException {
        return controllerClass.getDeclaredConstructors()[0].newInstance();
    }

    private void handleMetadata(HttpExchange exchange, String path, Map<String, String> requestQueryParams, Metadata metadata, Object instance) throws IOException {
        Map<String, String> metadataConfig = metadata.getConfig();

        if (metadataConfig.containsKey("path") && metadataConfig.get("path").equals(path)
                && exchange.getRequestMethod().equalsIgnoreCase(metadataConfig.get("method"))) {
            handleMethod(exchange, metadata, requestQueryParams, instance);
        }
    }

    private void handleMethod(HttpExchange exchange, Metadata metadata, Map<String, String> requestQueryParams, Object instance) throws IOException {
        Method method = findMatchingMethod(metadata, instance);

        if (method != null) {
            handleMatchingMethod(exchange, metadata, requestQueryParams, method, instance);
        } else {
            sendResponse(exchange, HTTPStatusCode.BAD_REQUEST, "Bad Request");
        }
    }

    private Method findMatchingMethod(Metadata metadata, Object instance) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.getName().equals(metadata.getMethodName()) && method.getReturnType().getName().equals(metadata.getReturnType())) {
                return method;
            }
        }
        return null;
    }

    private void handleMatchingMethod(HttpExchange exchange, Metadata metadata, Map<String, String> requestQueryParams,
                                      Method method, Object instance) throws IOException {
        List<Argument> expectedQueryParams = metadata.getArguments().stream()
                .filter(x -> x.getName().endsWith("Param"))
                .toList();

        if (requestQueryParams.size() == expectedQueryParams.size()) {
            exchange.getResponseHeaders().add("content-type",
                    metadata.getConfig().getOrDefault("content-type", "application/json"));
            invokeMethod(exchange, requestQueryParams, method, instance, metadata);
        } else {
            sendResponse(exchange, HTTPStatusCode.BAD_REQUEST, "Bad Request");
        }
    }

    private void invokeMethod(HttpExchange exchange, Map<String, String> requestQueryParams,
                              Method method, Object instance, Metadata metadata) throws IOException {
        try {
            Object response;

            if (!requestQueryParams.isEmpty() || hasRequestBody(metadata)) {
                Object[] methodParams = getMethodParams(method, exchange, metadata);
                response = method.invoke(instance, methodParams);
            } else {
                response = method.invoke(instance);
            }

            if (!metadata.getReturnType().equals("void")) {
                String json = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(response);
                sendResponse(exchange, HTTPStatusCode.OK, json);
            } else {
                sendResponse(exchange, HTTPStatusCode.OK, "OK");
            }
        } catch (Exception e) {
            sendResponse(exchange, HTTPStatusCode.INTERNAL_SERVER_ERROR, "Internal Server Error");
            LOGGER.error("Error processing request", e);
        }
    }

    private boolean hasRequestBody(Metadata metadata) {
        return metadata.getArguments().stream().anyMatch(argument -> argument.getName().endsWith("Body"));
    }

    private Object[] getMethodParams(Method method, HttpExchange exchange, Metadata metadata) throws IOException {
        Map<String, String> queryParams = getParametersFromQueryString(exchange.getRequestURI());
        Headers headers = exchange.getRequestHeaders();
        Map<String, byte[]> requestBodyParams = readRequestBodyParams(exchange);

        Object[] methodParams = new Object[method.getParameterCount()];
        int i = 0;

        for (Argument argument : metadata.getArguments()) {
            String paramName = argument.getName();

            if (paramName.endsWith("Param")) {
                String param = paramName.substring(0, paramName.indexOf("Param"));
                methodParams[i] = convertToType(queryParams.get(param), method.getParameterTypes()[i]);
            } else if (paramName.endsWith("Body")) {
                byte[] requestBody = requestBodyParams.get("body");

                if (requestBody != null) {
                    Class<?> paramType = method.getParameterTypes()[i];
                    methodParams[i] = convertRequestBodyToType(requestBody, paramType);
                } else {
                    methodParams[i] = null;
                }
            } else if (paramName.endsWith("Header")) {
                String param = paramName.substring(0, paramName.indexOf("Header"));

                if (headers.containsKey(param)) {
                    methodParams[i] = convertToType(headers.get(param).get(0), method.getParameterTypes()[i]);
                } else {
                    methodParams[i] = null;
                }
            }
            i++;
        }
        return methodParams;
    }

    private Map<String, byte[]> readRequestBodyParams(HttpExchange exchange) throws IOException {
        Map<String, byte[]> requestBodyParams = new HashMap<>();
        InputStream inputStream = exchange.getRequestBody();

        if (inputStream.available() > 0) {
            byte[] binaryData = readBinaryRequestBody(inputStream);
            requestBodyParams.put("body", binaryData);
        }
        return requestBodyParams;
    }

    private Object convertRequestBodyToType(byte[] requestBody, Class<?> targetType) throws IOException {
        if (targetType == byte[].class) {
            return requestBody;
        } else {
            return OBJECT_MAPPER.readValue(requestBody, targetType);
        }
    }

    private static byte[] readBinaryRequestBody(InputStream inputStream) throws IOException {
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        StringBuilder binaryContent = new StringBuilder();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            binaryContent.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return binaryContent.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static Object convertToType(String value, Class<?> targetType) {
        if (targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Float.class) {
            return Float.parseFloat(value);
        } else {
            return value;
        }
    }

    private static void sendResponse(HttpExchange exchange, HTTPStatusCode statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode.getCode(), response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static Map<String, String> getParametersFromQueryString(URI uri) {
        String query = uri.getQuery();
        Map<String, String> parameters = new HashMap<>();

        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");

                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return parameters;
    }
}
