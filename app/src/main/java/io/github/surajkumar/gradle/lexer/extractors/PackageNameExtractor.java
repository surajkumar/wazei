package io.github.surajkumar.gradle.lexer.extractors;

import io.github.surajkumar.gradle.lexer.token.Token;

import java.util.List;

public class PackageNameExtractor {

    public static String getPackageName(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            if (token.name().equals("PACKAGE_KEYWORD")) {
                sb.append(" ");
                continue;
            }
            if (!sb.isEmpty()) {
                if (token.name().equals("SEMICOLON")) {
                    break;
                }
                sb.append(token.value());
            }
        }
        return sb.toString().trim();
    }
}
