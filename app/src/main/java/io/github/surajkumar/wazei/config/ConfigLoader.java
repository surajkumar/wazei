package io.github.surajkumar.wazei.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static List<Config> loadConfig() throws IOException, URISyntaxException {
        URL resourceUrl = ConfigLoader.class.getResource("/wazei.json");
        if (resourceUrl != null) {
            String input = Files.readString(Path.of(resourceUrl.toURI()));
            TypeReference<List<Config>> typeReference = new TypeReference<>() {};
            return OBJECT_MAPPER.readValue(input, typeReference);
        }
        throw new RuntimeException("Resource not found: wazei.json");
    }
}
