package io.github.surajkumar.wazei;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for parsing query strings from a URI.
 *
 * @author Suraj Kumar
 */
public class QueryStringParser {

    /**
     * Extracts parameters from the query string of a given URI.
     *
     * @param uri
     *            The URI containing the query string.
     * @return A Map representing the key-value pairs of parameters extracted from
     *         the query string.
     */
    protected static Map<String, String> getParametersFromQueryString(URI uri) {
        String query = uri.getQuery();
        Map<String, String> parameters = new HashMap<>();
        if (query != null) {
            Arrays.stream(query.split("&"))
                    .map(pair -> pair.split("="))
                    .filter(keyValue -> keyValue.length == 2)
                    .forEach(
                            keyValue -> {
                                String key = decodeUrlComponent(keyValue[0]);
                                String value = decodeUrlComponent(keyValue[1]);
                                parameters.put(key, value);
                            });
        }
        return parameters;
    }

    /**
     * Decodes a URL component using UTF-8 encoding.
     *
     * @param component
     *            The URL component to be decoded.
     * @return The decoded string.
     */
    private static String decodeUrlComponent(String component) {
        return URLDecoder.decode(component, StandardCharsets.UTF_8);
    }
}
