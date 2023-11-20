package io.github.surajkumar.gradle.lexer;

import io.github.surajkumar.gradle.lexer.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaLexer implements Lexer {
    private final JavaTokenDescriptions tokenDescriptions;

    public JavaLexer() {
        tokenDescriptions = new JavaTokenDescriptions();
    }

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int position = 0;

        while (position < input.length()) {
            char currentChar = input.charAt(position);

            if (currentChar == ' '
                    || currentChar == '\t'
                    || currentChar == '\n'
                    || currentChar == '\r') {
                // Skip whitespace
                position++;
            } else if (currentChar == '/'
                    && (position + 1 < input.length())
                    && input.charAt(position + 1) == '/') {
                // Handle single-line comment
                int endOfLine = input.indexOf('\n', position);
                if (endOfLine == -1) {
                    endOfLine = input.length();
                }
                String comment = input.substring(position, endOfLine);
                tokens.add(new Token("SINGLE_LINE_COMMENT", comment, position));
                position = endOfLine;
            } else if (currentChar == '/'
                    && (position + 1 < input.length())
                    && input.charAt(position + 1) == '*') {
                // Handle multi-line comment
                int endComment = input.indexOf("*/", position + 2);
                if (endComment == -1) {
                    // Handle unterminated multi-line comment
                    tokens.add(new Token("UNKNOWN", "/*", position));
                    position += 2;
                } else {
                    endComment += 2; // Include the closing "*/"
                    String comment = input.substring(position, endComment);
                    tokens.add(new Token("MULTI_LINE_COMMENT", comment, position));
                    position = endComment;
                }
            } else if (currentChar == '"') {
                // Handle string literal
                int endOfString = position + 1;
                boolean escapeSequence = false;
                while (endOfString < input.length()
                        && (input.charAt(endOfString) != '"' || escapeSequence)) {
                    if (input.charAt(endOfString) == '\\') {
                        escapeSequence = !escapeSequence;
                    } else {
                        escapeSequence = false;
                    }
                    endOfString++;
                }

                if (endOfString < input.length() && input.charAt(endOfString) == '"') {
                    endOfString++;
                }

                String stringLiteral = input.substring(position, endOfString);
                tokens.add(new Token("STRING_LITERAL", stringLiteral, position));
                position = endOfString;
            } else {
                boolean matched = false;

                for (Map.Entry<String, Pattern> entry :
                        tokenDescriptions.getTokenDescription().getDescriptions().entrySet()) {
                    String identifier = entry.getKey();
                    Pattern pattern = entry.getValue();
                    Matcher matcher = pattern.matcher(input);
                    matcher.region(position, input.length());

                    if (matcher.lookingAt()) {
                        String match = matcher.group();
                        tokens.add(new Token(identifier, match, position));
                        position += match.length();
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    // Handle unrecognized characters or errors
                    char c = input.charAt(position);
                    tokens.add(new Token("UNKNOWN", String.valueOf(c), position));
                    position++;
                }
            }
        }

        return tokens;
    }
}
