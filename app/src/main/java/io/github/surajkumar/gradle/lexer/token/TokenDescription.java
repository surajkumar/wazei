package io.github.surajkumar.gradle.lexer.token;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TokenDescription {
    private final Map<String, Pattern> descriptions;

    public TokenDescription() {
        descriptions = new HashMap<>();
    }

    public void register(Pattern regex, String name) {
        if (descriptions.containsKey(name)) {
            throw new IllegalArgumentException(name + " has already been registered");
        }
        descriptions.put(name, regex);
    }

    public void remove(String name) {
        descriptions.remove(name);
    }

    public Pattern getRegex(String name) {
        return descriptions.get(name);
    }

    public Map<String, Pattern> getDescriptions() {
        return descriptions;
    }
}
