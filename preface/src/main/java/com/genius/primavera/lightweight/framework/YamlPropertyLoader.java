package com.genius.primavera.lightweight.framework;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Stack;

/**
 * 간단한 YAML 파일 로더
 * Spring Boot의 YamlPropertySourceLoader와 유사한 기능을 제공합니다.
 */
@Slf4j
public class YamlPropertyLoader {
    
    /**
     * YAML 파일을 Properties 객체로 변환합니다.
     */
    public static Properties loadYamlAsProperties(String resourceName) throws IOException {
        Properties properties = new Properties();
        
        var inputStream = YamlPropertyLoader.class.getClassLoader()
                .getResourceAsStream(resourceName);
        
        if (inputStream == null) {
            log.warn("YAML 파일을 찾을 수 없습니다: {}", resourceName);
            return properties;
        }
        
        try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            parseYaml(reader, properties);
            log.info("YAML 파일 로드 완료: {} (UTF-8)", resourceName);
        }
        
        return properties;
    }
    
    /**
     * YAML 내용을 파싱하여 Properties로 변환합니다.
     */
    private static void parseYaml(BufferedReader reader, Properties properties) throws IOException {
        Stack<String> keyStack = new Stack<>();
        Stack<Integer> indentStack = new Stack<>();
        indentStack.push(-1); // 루트 레벨
        
        String line;
        int lineNumber = 0;
        
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            
            // 빈 줄이나 주석 무시
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }
            
            try {
                parseLine(line, keyStack, indentStack, properties);
            } catch (Exception e) {
                log.warn("YAML 파싱 오류 (줄 {}): {}", lineNumber, e.getMessage());
            }
        }
    }
    
    /**
     * 한 줄씩 파싱합니다.
     */
    private static void parseLine(String line, Stack<String> keyStack, Stack<Integer> indentStack, Properties properties) {
        int currentIndent = getIndentLevel(line);
        String trimmedLine = line.trim();
        
        // 현재 들여쓰기 레벨에 맞게 스택 정리
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
                // 중첩된 키 (값이 없는 경우)
                keyStack.push(key);
                indentStack.push(currentIndent);
            } else {
                // 키-값 쌍
                String fullKey = buildFullKey(keyStack, key);
                properties.setProperty(fullKey, value);
                log.debug("YAML 속성 추가: {} = {}", fullKey, value);
            }
        }
    }
    
    /**
     * 들여쓰기 레벨을 계산합니다.
     */
    private static int getIndentLevel(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indent++;
            } else if (c == '\t') {
                indent += 4; // 탭을 4개 공백으로 간주
            } else {
                break;
            }
        }
        return indent;
    }
    
    /**
     * 전체 키 경로를 생성합니다.
     */
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