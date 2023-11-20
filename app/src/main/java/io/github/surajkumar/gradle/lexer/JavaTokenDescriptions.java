package io.github.surajkumar.gradle.lexer;

import io.github.surajkumar.gradle.lexer.token.TokenDescription;
import java.util.Set;
import java.util.regex.Pattern;

public class JavaTokenDescriptions {
    private final TokenDescription tokenDescription;

    public JavaTokenDescriptions() {
        tokenDescription = new TokenDescription();

        /* Comments */
        tokenDescription.register(
                Pattern.compile("/\\*([^*]|(\\*+[^*/]))*\\*+/"), "MULTI_LINE_COMMENT");
        tokenDescription.register(Pattern.compile("//.*"), "SINGLE_LINE_COMMENT");

        /* Keywords */
        tokenDescription.register(
                Pattern.compile("public|private|protected"), "ACCESS_MODIFIER_KEYWORD");
        tokenDescription.register(Pattern.compile("int|double|float|String"), "DATA_TYPE_KEYWORD");
        // tokenDescription.register(Pattern.compile("exports|opens|to|transitive"),
        // "MODULE_DIRECTIVE");
        tokenDescription.register(Pattern.compile("if"), "IF_KEYWORD");
        tokenDescription.register(Pattern.compile("else"), "ELSE_KEYWORD");
        tokenDescription.register(Pattern.compile("while"), "WHILE_KEYWORD");
        tokenDescription.register(Pattern.compile("for"), "FOR_KEYWORD");
        tokenDescription.register(Pattern.compile("return"), "RETURN_KEYWORD");
        tokenDescription.register(Pattern.compile("class"), "CLASS_KEYWORD");
        tokenDescription.register(Pattern.compile("static"), "STATIC_KEYWORD");
        tokenDescription.register(Pattern.compile("void"), "VOID_KEYWORD");
        tokenDescription.register(Pattern.compile("extends"), "EXTENDS_KEYWORD");
        tokenDescription.register(Pattern.compile("implements"), "IMPLEMENTS_KEYWORD");
        tokenDescription.register(Pattern.compile("new"), "NEW_KEYWORD");
        tokenDescription.register(Pattern.compile("break"), "BREAK_KEYWORD");
        tokenDescription.register(Pattern.compile("continue"), "CONTINUE_KEYWORD");
        tokenDescription.register(Pattern.compile("default"), "DEFAULT_KEYWORD");
        tokenDescription.register(Pattern.compile("try"), "TRY_KEYWORD");
        tokenDescription.register(Pattern.compile("catch"), "CATCH_KEYWORD");
        tokenDescription.register(Pattern.compile("record"), "RECORD_KEYWORD");
        tokenDescription.register(Pattern.compile("volatile"), "VOLATILE_KEYWORD");
        tokenDescription.register(Pattern.compile("synchronized"), "SYNCHRONIZED_KEYWORD");
        tokenDescription.register(Pattern.compile("abstract"), "ABSTRACT_KEYWORD");
        tokenDescription.register(Pattern.compile("interface"), "INTERFACE_KEYWORD");
        tokenDescription.register(Pattern.compile("assert"), "ASSERT_KEYWORD");
        tokenDescription.register(Pattern.compile("case"), "CASE_KEYWORD");
        tokenDescription.register(Pattern.compile("char"), "CHAR_KEYWORD");
        tokenDescription.register(Pattern.compile("do"), "DO_KEYWORD");
        tokenDescription.register(Pattern.compile("final"), "FINAL_KEYWORD");
        tokenDescription.register(Pattern.compile("finally"), "FINALLY_KEYWORD");
        tokenDescription.register(Pattern.compile("import"), "IMPORT_KEYWORD");
        tokenDescription.register(Pattern.compile("instanceof"), "INSTANCEOF_KEYWORD");
        tokenDescription.register(Pattern.compile("native"), "NATIVE_KEYWORD");
        tokenDescription.register(Pattern.compile("package"), "PACKAGE_KEYWORD");
        tokenDescription.register(Pattern.compile("strictfp"), "STRICTFP_KEYWORD");
        tokenDescription.register(Pattern.compile("this"), "THIS_KEYWORD");
        tokenDescription.register(Pattern.compile("throw"), "THROW_KEYWORD");
        tokenDescription.register(Pattern.compile("throws"), "THROWS_KEYWORD");
        tokenDescription.register(Pattern.compile("transient"), "TRANSIENT_KEYWORD");
        tokenDescription.register(Pattern.compile("null"), "NULL_KEYWORD");
        tokenDescription.register(Pattern.compile("const"), "CONST_KEYWORD");
        tokenDescription.register(Pattern.compile("goto"), "GOTO_KEYWORD");
        tokenDescription.register(Pattern.compile("module"), "MODULE_KEYWORD");
        tokenDescription.register(Pattern.compile("var"), "VAR_KEYWORD");

        /* Literals */
        tokenDescription.register(Pattern.compile("0[xX][0-9a-fA-F]+"), "HEX_LITERAL");
        tokenDescription.register(Pattern.compile("0[0-7]+"), "OCTAL_LITERAL");
        tokenDescription.register(Pattern.compile("\\d+(\\.\\d+)?([eE][+-]?\\d+)?"), "NUMBER");
        tokenDescription.register(Pattern.compile("'.'"), "CHARACTER_LITERAL");
        tokenDescription.register(Pattern.compile("true|false"), "BOOLEAN_LITERAL");

        /* Operators */
        // Compound Assignment
        tokenDescription.register(
                Pattern.compile("\\+=|-=|\\*=|/=|%=|\\^="), "COMPOUND_ASSIGNMENT_OPERATOR");
        tokenDescription.register(Pattern.compile("\\+"), "PLUS_OPERATOR");
        tokenDescription.register(Pattern.compile("-"), "MINUS_OPERATOR");
        tokenDescription.register(Pattern.compile("\\*"), "MULTIPLICATION_OPERATOR");
        tokenDescription.register(Pattern.compile("/"), "DIVISION_OPERATOR");
        tokenDescription.register(Pattern.compile("\\^"), "EXPONENT_OPERATOR");
        tokenDescription.register(Pattern.compile("%"), "MODULO_OPERATOR");

        // Unary
        tokenDescription.register(Pattern.compile("!"), "NOT_OPERATOR");
        tokenDescription.register(Pattern.compile("\\+\\+|--"), "INCREMENT_OR_DECREMENT");

        // Assignment
        tokenDescription.register(Pattern.compile("="), "ASSIGNMENT_OPERATOR");

        // Relational
        tokenDescription.register(Pattern.compile("==|!=|<=|>=|<|>"), "COMPARISON_OPERATOR");
        tokenDescription.register(Pattern.compile("<"), "OPEN_ANGLE_BRACKET");
        tokenDescription.register(Pattern.compile(">"), "CLOSE_ANGLE_BRACKET");

        // Logical
        tokenDescription.register(Pattern.compile("&&"), "LOGICAL_AND_OPERATOR");
        tokenDescription.register(Pattern.compile("\\|\\|"), "LOGICAL_OR_OPERATOR");

        // Ternary
        tokenDescription.register(Pattern.compile(":"), "COLON_OPERATOR");
        tokenDescription.register(Pattern.compile("\\?"), "QUESTION_MARK_OPERATOR");

        // Bitwise
        tokenDescription.register(Pattern.compile("&|\\||\\^|~"), "BITWISE_OPERATOR");

        // Shift
        tokenDescription.register(Pattern.compile("<<|>>"), "BITWISE_SHIFT_OPERATOR");

        /* Separators */
        tokenDescription.register(Pattern.compile(";"), "SEMICOLON");
        tokenDescription.register(Pattern.compile(","), "COMMA_SEPARATOR");
        tokenDescription.register(Pattern.compile("\\."), "DOT_SEPARATOR");
        tokenDescription.register(Pattern.compile("\\s+"), "WHITESPACE_SEPARATOR");
        tokenDescription.register(Pattern.compile("\\("), "OPEN_PARENTHESIS_SEPARATOR");
        tokenDescription.register(Pattern.compile("\\)"), "CLOSE_PARENTHESIS_SEPARATOR");
        tokenDescription.register(Pattern.compile("\\{"), "OPEN_CURLY_BRACE_SEPARATOR");
        tokenDescription.register(Pattern.compile("}"), "CLOSE_CURLY_BRACE_SEPARATOR");
        tokenDescription.register(Pattern.compile("\\["), "OPEN_SQUARE_BRACKET_SEPARATOR");
        tokenDescription.register(Pattern.compile("]"), "CLOSE_SQUARE_BRACKET_SEPARATOR");

        /* Other */
        tokenDescription.register(Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*"), "ANNOTATION");
        tokenDescription.register(Pattern.compile("\\?\\."), "NULL_SAFE_NAVIGATION");
        tokenDescription.register(Pattern.compile("\\.\\.\\."), "VARARGS");
        tokenDescription.register(Pattern.compile("->"), "LAMBDA_ARROW");
        tokenDescription.register(Pattern.compile("::"), "METHOD_REFERENCE");
        tokenDescription.register(
                Pattern.compile("\\\\u[0-9A-Fa-f]{4}"), "UNICODE_ESCAPE_SEQUENCE");
        tokenDescription.register(Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*"), "IDENTIFIER");
    }

    public Set<String> getIdentifiers() {
        return tokenDescription.getDescriptions().keySet();
    }

    public TokenDescription getTokenDescription() {
        return tokenDescription;
    }
}
