package com.genius.primavera.lightweight.framework;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Stack;

@Slf4j
public class YamlPropertyLoader {

    public static Properties loadYamlAsProperties(String resourceName) throws IOException {
        Properties properties = new Properties();
        
        var inputStream = YamlPropertyLoader.class.getClassLoader()
                .getResourceAsStream(resourceName);
        
        if (inputStream == null) {
            log.warn("YAML connection test should file: {}", resourceName);
            return properties;
        }
        
        try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            parseYaml(reader, properties);
            log.info("YAML test completed: {} (UTF-8)", resourceName);
        }
        
        return properties;
    }

    private static void parseYaml(BufferedReader reader, Properties properties) throws IOException {
        Stack<String> keyStack = new Stack<>();
        Stack<Integer> indentStack = new Stack<>();
        indentStack.push(-1);
        
        String line;
        int lineNumber = 0;
        
        while ((line = reader.readLine()) != null) {
            lineNumber++;

            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }
            
            try {
                parseLine(line, keyStack, indentStack, properties);
            } catch (Exception e) {
                log.warn("YAML test error (should {}): {}", lineNumber, e.getMessage());
            }
        }
    }

    private static void parseLine(String line, Stack<String> keyStack, Stack<Integer> indentStack, Properties properties) {
        int currentIndent = getIndentLevel(line);
        String trimmedLine = line.trim();

        while (indentStack.size() > 1 && currentIndent <= indentStack.peek()) {
            indentStack.pop();
            if (!keyStack.isEmpty()) {
                keyStack.pop();
            }
        }
        
        if (trimmedLine.contains(":")) {
            String[] parts = trimmedLine.split(":", 2);
            String key = parts[0].trim();
            String value = parts.length > 1 ? parts[1].trim() : "";
            
            if (value.isEmpty()) {

                keyStack.push(key);
                indentStack.push(currentIndent);
            } else {

                String fullKey = buildFullKey(keyStack, key);
                properties.setProperty(fullKey, value);
                log.debug("YAML test: {} = {}", fullKey, value);
            }
        }
    }

    private static int getIndentLevel(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indent++;
            } else if (c == '\t') {
                indent += 4;
            } else {
                break;
            }
        }
        return indent;
    }

    private static String buildFullKey(Stack<String> keyStack, String currentKey) {
        if (keyStack.isEmpty()) {
            return currentKey;
        }
        
        StringBuilder fullKey = new StringBuilder();
        for (String key : keyStack) {
            if (fullKey.length() > 0) {
                fullKey.append(".");
            }
            fullKey.append(key);
        }
        
        if (fullKey.length() > 0) {
            fullKey.append(".");
        }
        fullKey.append(currentKey);
        
        return fullKey.toString();
    }
}