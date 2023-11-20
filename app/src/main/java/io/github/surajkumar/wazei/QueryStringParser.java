package io.github.surajkumar.wazei;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class QueryStringParser {

    protected static Map<String, String> getParametersFromQueryString(URI uri) {
        String query = uri.getQuery();
        Map<String, String> parameters = new HashMap<>();
        if (query != null) {
            Arrays.stream(query.split("&"))
                    .map(pair -> pair.split("="))
                    .filter(keyValue -> keyValue.length == 2)
                    .forEach(keyValue -> {
                        String key = decodeUrlComponent(keyValue[0]);
                        String value = decodeUrlComponent(keyValue[1]);
                        parameters.put(key, value);
                    });
        }
        return parameters;
    }

    private static String decodeUrlComponent(String component) {
        return URLDecoder.decode(component, StandardCharsets.UTF_8);
    }
}
