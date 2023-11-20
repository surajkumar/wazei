package io.github.surajkumar.gradle.lexer.extractors;

import io.github.surajkumar.gradle.lexer.token.Token;

import java.util.ArrayList;
import java.util.List;

public class MethodExtractor {
    public static List<Method> getMethods(List<Token> tokens) {
        List<Method> methods = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.name().equals("ACCESS_MODIFIER_KEYWORD")
                    && token.value().equals("public")) { // access
                Token expectedDocumentation = tokens.get(i - 1);
                int cursor = i;
                Token type = tokens.get(++cursor);
                if (type.name().equals("DATA_TYPE_KEYWORD") // type
                        || type.name().equals("VOID_KEYWORD")
                        || type.name().equals("IDENTIFIER")) {
                    Token name = tokens.get(++cursor);
                    if (name.name().equals("IDENTIFIER")) { // name
                        Token openParam = tokens.get(++cursor);
                        if (!openParam.name().equals("OPEN_PARENTHESIS_SEPARATOR")) {
                            throw new RuntimeException(
                                    "Syntax error occurred near " + openParam.value());
                        }
                        List<Argument> arguments = new ArrayList<>();
                        Token next = tokens.get(++cursor);
                        while (!next.name().equals("CLOSE_PARENTHESIS_SEPARATOR")) {
                            if (next.name().equals("DATA_TYPE_KEYWORD")
                                    || next.name().equals("IDENTIFIER")) {
                                String argType = next.value();
                                next = tokens.get(++cursor);
                                if (!next.name().equals("IDENTIFIER")) {
                                    throw new RuntimeException(
                                            "Syntax error occurred near " + next.value());
                                }
                                arguments.add(new Argument(argType, next.value()));
                            }
                            next = tokens.get(++cursor);
                        }

                        String documentation = "";
                        if (expectedDocumentation.name().equals("MULTI_LINE_COMMENT")) {
                            documentation = expectedDocumentation.value();
                        }

                        Method method =
                                new Method(
                                        i,
                                        documentation,
                                        token.value(),
                                        type.value(),
                                        name.value(),
                                        arguments);
                        methods.add(method);
                    }
                }
            }
        }
        return methods;
    }

    public record Method(
            int index,
            String documentation,
            String access,
            String type,
            String name,
            List<Argument> arguments) {}

    public record Argument(String type, String name) {}
}
