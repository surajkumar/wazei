package io.github.surajkumar.gradle.lexer.token;

public record Token(String name, String value, int position) {
    @Override
    public String toString() {
        return (name + " " + value).trim();
    }
}
