package io.github.surajkumar.gradle.lexer.extractors;

import io.github.surajkumar.gradle.lexer.token.Token;

import java.util.ArrayList;
import java.util.List;

public class ClassExtractor {

    public static String getClassName(List<Token> tokens) {
        for(int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if(token.name().equals("CLASS_KEYWORD") && (i < tokens.size() - 1)) {
                Token expectedIdentifier = tokens.get(i+1);
                if(expectedIdentifier.name().equals("IDENTIFIER")) {
                    return expectedIdentifier.value();
                }
            }
        }
        return "";
    }

    public static List<String> getImports(List<Token> tokens) {
        List<String> imports = new ArrayList<>();
        for(int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if(token.name().equals("CLASS_KEYWORD")) {
                break;
            }
            if(token.name().equals("IMPORT_KEYWORD")) {
                token = tokens.get(++i);
                StringBuilder sb = new StringBuilder();
                while(!token.name().equals("SEMICOLON")) {
                    sb.append(token.value());
                    token = tokens.get(++i);
                }
                imports.add(sb.toString());
            }
        }
        return imports;
    }

    public static String getClassComments(List<Token> tokens) {
        int stopIndex = 0;
        for(int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if(token.name().equals("CLASS_KEYWORD") && (i < tokens.size() - 1)) {
                stopIndex = i;
                break;
            }
        }
        for(int i = 0; i < stopIndex; i++) {
            Token token = tokens.get(i);
            if(token.name().equals("MULTI_LINE_COMMENT")) {
                return token.value();
            }
        }
        return "";
    }
}
