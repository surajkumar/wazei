package io.github.surajkumar.wazei.noconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.github.surajkumar.wazei.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class NoConfigRequestHandler implements HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoConfigRequestHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final List<Class<?>> classes;

    public NoConfigRequestHandler() throws IOException {
        this.classes =
                ClassScanner.findClasses().stream()
                        .filter(c -> c.getName().endsWith("Controller"))
                        .toList();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String remoteAddress = exchange.getRemoteAddress().getHostString();
        LOGGER.info("Received request from {} for path: {}", remoteAddress, path);

        String[] parts = path.substring(1).split("/");
        if (parts.length != 2) {
            sendResponse(exchange, 404, new HashMap<>(), "Page not found");
            return;
        }

        Map<String, String> queryParameters =
                QueryStringParser.getParametersFromQueryString(exchange.getRequestURI());

        List<String> queryParametersList = new ArrayList<>(queryParameters.values());

        String controller = parts[0];
        String requestedMethod = parts[1];

        Class<?> clazz = findClass(controller);
        if (clazz == null) {
            sendResponse(exchange, 404, new HashMap<>(), "Page not found");
            return;
        }

        Method method = null;

        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equalsIgnoreCase(requestedMethod)) {
                method = m;
                break;
            }
        }

        if (method == null) {
            sendResponse(exchange, 404, new HashMap<>(), "Page not found");
            return;
        }

        Object[] methodParams = new Object[method.getParameterCount()];

        for (int i = 0; i < method.getParameterCount(); i++) {
            Parameter p = method.getParameters()[i];
            if (p.getType().getName().contains("java.lang")
                    || !p.getType().getName().contains(".")) {
                methodParams[i] =
                        RequestProcessor.convertToType(queryParametersList.get(i), p.getType());
            } else {
                byte[] requestBody =
                        RequestProcessor.readBinaryRequestBody(exchange.getRequestBody());
                methodParams[i] =
                        RequestProcessor.convertRequestBodyToType(requestBody, p.getType());
            }
        }

        try {
            if (method.getReturnType().getName().equals("void")) {
                MethodInvoker.invokeVoidMethod(
                        ClassInstantiator.create(clazz), method, methodParams);
                sendResponse(exchange, 200, new HashMap<>(), "");
            } else {
                MethodResponse methodResponse =
                        MethodInvoker.invokeMethod(
                                ClassInstantiator.create(clazz), method, methodParams);
                RequestHandler.sendJsonResponse(
                        exchange, HTTPStatusCode.OK, methodResponse.getResponse());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> findClass(String name) {
        for (Class<?> clazz : classes) {
            if (clazz.getName().toUpperCase().endsWith(name.toUpperCase())) {
                return clazz;
            }
        }
        return null;
    }

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
