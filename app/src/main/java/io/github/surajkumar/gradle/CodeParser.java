package io.github.surajkumar.gradle;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.surajkumar.gradle.lexer.JavaLexer;
import io.github.surajkumar.gradle.lexer.Lexer;
import io.github.surajkumar.gradle.lexer.extractors.ClassExtractor;
import io.github.surajkumar.gradle.lexer.extractors.MethodExtractor;
import io.github.surajkumar.gradle.lexer.extractors.PackageNameExtractor;
import io.github.surajkumar.gradle.lexer.token.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeParser.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<Config> CONFIGS = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        LOGGER.info("Scanning src/main");

        Files.walk(Path.of("src/main"))
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java"))
                .forEach(file -> {
                    String source = null;
                    try {
                        source = Files.readString(file);
                    } catch (IOException e) {
                        LOGGER.warn("Could not open {}: {}", file.getFileName(), e.getMessage());
                    }
                    if(source != null) {
                        run(source);
                    }
                });

        Files.writeString(
                Path.of("src/main/resources/wazei.json"),
                OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(CONFIGS));

        Thread.sleep(100);
    }

    private static void run(String sourceCode) {
        Lexer tokenizer = new JavaLexer();
        List<Token> tokens = tokenizer.tokenize(sourceCode);
        String packageName = PackageNameExtractor.getPackageName(tokens);
        String className = ClassExtractor.getClassName(tokens);
        if(className.endsWith("Controller")) {
            LOGGER.info("Parsing {}.{}", packageName, className);
            List<String> classImports = ClassExtractor.getImports(tokens);
            fixImports(packageName, classImports);
            List<MethodExtractor.Method> methods = MethodExtractor.getMethods(tokens);
            List<Metadata> metadata = new ArrayList<>();
            for(MethodExtractor.Method method : methods) {
                if(method.access().equals("public")) {
                    Map<String, String> keys = extractKeyValuePairs(method.documentation());
                    if(!keys.isEmpty()) {
                        if(!checkParameterNames(method.arguments())) {
                            throw new RuntimeException(
                                    "Method parameter names must end with either Header, Param or Body, error in %s.%s#%s"
                                            .formatted(packageName, className, method.name()));
                        }
                        metadata.add(new Metadata(method.name(), fixType(packageName, method.type()), fixArgs(packageName, method.arguments()), keys));
                    }
                } else {
                    LOGGER.trace("Skipping non-public method " + method.name());
                }
            }
            CONFIGS.add(new Config(packageName, className, classImports, metadata));
        }
    }

    private static List<MethodExtractor.Argument> fixArgs(String packageName, List<MethodExtractor.Argument> args) {
        List<MethodExtractor.Argument> arguments = new ArrayList<>();
        for (MethodExtractor.Argument arg : args) {
            String type = fixType(packageName, arg.type());
            arguments.add(new MethodExtractor.Argument(type, arg.name()));
        }
        return arguments;
    }

    private static String fixType(String packageName, String type) {
        if (!type.contains(".") && !type.equals("void") && !isPrimitive(type)) {
            String fullyQualifiedImport;
            if (isStandardLibraryClass(type)) {
                fullyQualifiedImport = "java.lang." + type;
            } else {
                fullyQualifiedImport = packageName + "." + type;
            }
            return fullyQualifiedImport;
        }
        return type;
    }

    private static void fixImports(String packageName, List<String> imports) {
        imports.replaceAll(entry -> fixType(packageName, entry));
    }

    private static boolean isStandardLibraryClass(String className) {
        return className.equals("Integer")
                || className.equals("String")
                || className.equals("Short")
                || className.equals("Long")
                || className.equals("Float")
                || className.equals("Double")
                || className.equals("Character")
                || className.equals("Boolean")
                || className.equals("Byte")
                || className.equals("Math")
                || className.equals("StringBuilder")
                || className.equals("Object")
                || className.equals("Thread")
                || className.equals("Runnable")
                || className.equals("System")
                || className.equals("ClassLoader")
                || className.equals("Enum")
                ;
    }

    private static boolean isPrimitive(String className) {
        return className.equals("int")
                || className.equals("short")
                || className.equals("long")
                || className.equals("float")
                || className.equals("double")
                || className.equals("char")
                || className.equals("boolean")
                || className.equals("byte")
                ;
    }


    private static boolean checkParameterNames(List<MethodExtractor.Argument> arguments) {
        for(MethodExtractor.Argument argument : arguments) {
            boolean matchedPrefix = false;
            String name = argument.name();
            for(MethodNamePrefix prefix : MethodNamePrefix.values()) {
                if(name.endsWith(prefix.toString())) {
                    matchedPrefix = true;
                    break;
                }
            }
            if(!matchedPrefix) {
                return false;
            }
        }
        return true;
    }

    private record Config(String packageName,
                          String className,
                          List<String> imports,
                          List<Metadata> metadata) {}

    private record Metadata(String methodName,
                        String returnType,
                        List<MethodExtractor.Argument> arguments,
                        Map<String, String> config
                        ) {}

    private static Map<String, String> extractKeyValuePairs(String input) {
        Map<String, String> keyValuePairs = new HashMap<>();
        Pattern pattern = Pattern.compile("---(.*?)---", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String sectionContent = matcher.group(1).trim();
            Pattern keyValuePattern = Pattern.compile("\\$([\\w-]+)=(\\S+)");
            Matcher keyValueMatcher = keyValuePattern.matcher(sectionContent);
            while (keyValueMatcher.find()) {
                String key = keyValueMatcher.group(1);
                String value = keyValueMatcher.group(2);
                keyValuePairs.put(key, value);
            }
        }
        return keyValuePairs;
    }
}
