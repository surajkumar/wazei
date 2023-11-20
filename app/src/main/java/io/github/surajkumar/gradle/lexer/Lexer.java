package io.github.surajkumar.gradle.lexer;

import io.github.surajkumar.gradle.lexer.token.Token;
import java.util.List;

public interface Lexer {
    List<Token> tokenize(String input);
}
