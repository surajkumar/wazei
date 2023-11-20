package io.github.surajkumar.wazei;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;

import io.github.surajkumar.wazei.config.Argument;
import io.github.surajkumar.wazei.config.Config;
import io.github.surajkumar.wazei.config.Metadata;
import io.github.surajkumar.wazei.exceptions.MethodInvocationException;
import io.github.surajkumar.wazei.exceptions.UnreadableRequestBodyException;

import java.io.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class RequestProcessor {
    private static final String PATH_KEY = "path";
    private static final String METHOD_KEY = "method";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Config config;

    public RequestProcessor(Config config) {
        this.config = config;
    }

    public MethodResponse processRequest(
            String path,
            String httpMethod,
            Map<String, String> params,
            Headers headers,
            InputStream body)
            throws MethodInvocationException, UnreadableRequestBodyException,
                    ClassNotFoundException {
        try {
            Class<?> controllerClass = Class.forName(
                    "%s.%s".formatted(config.getPackageName(), config.getClassName()));
            List<Metadata> metadataList = config.getMetadata();
            for (Metadata metadata : metadataList) {
                Map<String, String> metadataConfig = metadata.getConfig();
                if (path.equals(metadataConfig.get(PATH_KEY))
                        && httpMethod.equalsIgnoreCase(metadataConfig.get(METHOD_KEY))) {
                    List<Argument> expectedParams = metadata.getArguments().stream()
                            .filter(x -> x.getName().endsWith("Param"))
                            .toList();
                    String methodName = metadata.getMethodName();
                    String returnType = metadata.getReturnType();
                    Method method = MethodInvoker.findMatchingMethod(
                            controllerClass, methodName, returnType);
                    Object instance;
                    try {
                        instance = controllerClass.getDeclaredConstructors()[0].newInstance();
                    } catch (Exception e) {
                        throw new MethodInvocationException(
                                "Unable to construct instance: " + e.getMessage());
                    }
                    if (method != null && params.size() == expectedParams.size()) {
                        try {
                            Object[] methodParams = getMethodParams(
                                    method, headers, params, body, metadata.getArguments());
                            if (metadata.getReturnType().equals("void")) {
                                MethodInvoker.invokeVoidMethod(instance, method, methodParams);
                                return new MethodResponse();
                            } else {
                                return MethodInvoker.invokeMethod(instance, method, methodParams);
                            }
                        } catch (IOException ex) {
                            throw new UnreadableRequestBodyException(ex.getMessage());
                        } catch (Exception e) {
                            throw new MethodInvocationException(e.getMessage());
                        }
                    } else {
                        throw new MethodInvocationException("Method not found");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Controller class not found: " + e.getMessage());
        }
        return null;
    }

    private Object[] getMethodParams(
            Method method,
            Headers headers,
            Map<String, String> queryParams,
            InputStream body,
            List<Argument> arguments)
            throws IOException {
        Object[] methodParams = new Object[method.getParameterCount()];
        for (int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            String paramName = argument.getName();
            Class<?> paramType = method.getParameterTypes()[i];
            if (paramName.endsWith("Param")) {
                String param = paramName.substring(0, paramName.indexOf("Param"));
                methodParams[i] = convertToType(queryParams.get(param), paramType);
            } else if (paramName.endsWith("Body")) {
                byte[] requestBody = readBinaryRequestBody(body);
                methodParams[i] = convertRequestBodyToType(requestBody, paramType);
            } else if (paramName.endsWith("Header")) {
                String param = paramName.substring(0, paramName.indexOf("Header"));
                methodParams[i] = headers.containsKey(param)
                        ? convertToType(headers.get(param).get(0), paramType)
                        : null;
            }
        }
        return methodParams;
    }

    private byte[] readBinaryRequestBody(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, bytesRead);
            }
            return result.toByteArray();
        }
    }

    private Object convertRequestBodyToType(byte[] requestBody, Class<?> targetType)
            throws IOException {
        if (targetType == byte[].class) {
            return requestBody;
        } else {
            return OBJECT_MAPPER.readValue(requestBody, targetType);
        }
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
}
