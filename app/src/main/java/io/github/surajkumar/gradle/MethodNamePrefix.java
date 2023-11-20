package io.github.surajkumar.gradle;

public enum MethodNamePrefix {
    HEADER("Header"),
    PARAMETER("Param"),
    BODY("Body");

    private final String prefix;

    MethodNamePrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }
}
