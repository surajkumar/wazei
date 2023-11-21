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

/**
 * Processes incoming HTTP requests based on the provided configuration.
 *
 * @author Suraj Kumar
 */
public class RequestProcessor {
    private static final String PATH_KEY = "path";
    private static final String METHOD_KEY = "method";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Config config;

    /**
     * Constructs a RequestProcessor with the given configuration.
     *
     * @param config The configuration used for processing requests.
     */
    public RequestProcessor(Config config) {
        this.config = config;
    }

    /**
     * Processes an incoming HTTP request.
     *
     * @param path The request path.
     * @param httpMethod The HTTP method (GET, POST, etc.).
     * @param params The query parameters.
     * @param headers The request headers.
     * @param body The request body as an InputStream.
     * @return A MethodResponse containing the result of processing the request.
     * @throws MethodInvocationException If an error occurs during method invocation.
     * @throws UnreadableRequestBodyException If the request body cannot be read.
     * @throws ClassNotFoundException If the controller class is not found.
     */
    public MethodResponse processRequest(
            String path,
            String httpMethod,
            Map<String, String> params,
            Headers headers,
            InputStream body)
            throws MethodInvocationException,
                    UnreadableRequestBodyException,
                    ClassNotFoundException {
        try {
            // Load the Controller class
            Class<?> controllerClass =
                    Class.forName(
                            "%s.%s".formatted(config.getPackageName(), config.getClassName()));
            List<Metadata> metadataList = config.getMetadata();
            for (Metadata metadata : metadataList) {
                Map<String, String> metadataConfig = metadata.getConfig();

                // When we found the metadata for the path
                if (path.equals(metadataConfig.get(PATH_KEY))
                        && httpMethod.equalsIgnoreCase(metadataConfig.get(METHOD_KEY))) {

                    // Filter out the method arguments that are to be used from the query string
                    List<Argument> expectedParams =
                            metadata.getArguments().stream()
                                    .filter(x -> x.getName().endsWith("Param"))
                                    .toList();
                    String methodName = metadata.getMethodName();
                    String returnType = metadata.getReturnType();
                    Method method =
                            MethodInvoker.findMatchingMethod(
                                    controllerClass, methodName, returnType);
                    Object instance = ClassInstantiator.create(controllerClass);

                    // Check that the method we found matches what we expect for query params
                    // This will not take into consideration other param types e.g. Header and Body
                    if (method != null && params.size() == expectedParams.size()) {
                        try {
                            Object[] methodParams =
                                    getMethodParams(
                                            method, headers, params, body, metadata.getArguments());
                            if (metadata.getReturnType().equals("void")) {
                                // Invoke a void method i.e. has no return type
                                MethodInvoker.invokeVoidMethod(instance, method, methodParams);
                                return new MethodResponse();
                            } else {
                                // Invoke a method that has a return type
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

    /**
     * Converts request parameters, headers, and body into an array of method parameters.
     *
     * @param method The method being invoked.
     * @param headers The request headers.
     * @param queryParams The query parameters.
     * @param body The request body as an InputStream.
     * @param arguments The list of method arguments and their types.
     * @return An array of method parameters.
     * @throws IOException If an error occurs during parameter conversion.
     */
    private Object[] getMethodParams(
            Method method,
            Headers headers,
            Map<String, String> queryParams,
            InputStream body,
            List<Argument> arguments)
            throws IOException {
        Object[] methodParams = new Object[method.getParameterCount()];

        // Loop over the arguments we expect the method to have
        for (int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            String paramName = argument.getName();
            Class<?> paramType = method.getParameterTypes()[i];
            if (paramName.endsWith("Param")) {
                // Use query parameter for this method parameter
                String param = paramName.substring(0, paramName.indexOf("Param"));
                methodParams[i] = convertToType(queryParams.get(param), paramType);
            } else if (paramName.endsWith("Body")) {
                // Use the request body for this method parameter
                byte[] requestBody = readBinaryRequestBody(body);
                methodParams[i] = convertRequestBodyToType(requestBody, paramType);
            } else if (paramName.endsWith("Header")) {
                // Use the request header for this method parameter
                String param = paramName.substring(0, paramName.indexOf("Header"));
                methodParams[i] =
                        headers.containsKey(param)
                                ? convertToType(headers.get(param).get(0), paramType)
                                : null;
            }
        }
        return methodParams;
    }

    /**
     * Reads the binary request body into a byte array.
     *
     * @param inputStream The request body as an InputStream.
     * @return The request body as a byte array.
     * @throws IOException If an error occurs while reading the request body.
     */
    public static byte[] readBinaryRequestBody(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, bytesRead);
            }
            return result.toByteArray();
        }
    }

    /**
     * Converts the request body to the specified type.
     *
     * @param requestBody The request body as a byte array.
     * @param targetType The target type to convert the request body to.
     * @return The converted object.
     * @throws IOException If an error occurs during conversion.
     */
    public static Object convertRequestBodyToType(byte[] requestBody, Class<?> targetType)
            throws IOException {
        if (targetType == byte[].class) {
            return requestBody;
        } else {
            return OBJECT_MAPPER.readValue(requestBody, targetType);
        }
    }

    /**
     * Converts a string value to the specified type. This is used as part of the logic of creating
     * the method parameters. It is expected that header and query string parameters are primitives.
     *
     * @param value The string value to convert.
     * @param targetType The target type to convert the value to.
     * @return The converted object.
     */
    public static Object convertToType(String value, Class<?> targetType) {
        if (targetType == Integer.class || targetType.getName().equals("int")) {
            return Integer.parseInt(value);
        } else if (targetType == Double.class || targetType.getName().equals("double")) {
            return Double.parseDouble(value);
        } else if (targetType == Float.class || targetType.getName().equals("float")) {
            return Float.parseFloat(value);
        } else if (targetType == String.class) {
            return value;
        } else {
            throw new RuntimeException("Unknown type: " + targetType);
        }
    }
}
