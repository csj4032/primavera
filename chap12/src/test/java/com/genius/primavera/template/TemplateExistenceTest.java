package com.genius.primavera.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 템플릿 파일 존재 여부 검증 테스트
 * 
 * 실제 렌더링은 하지 않고, 템플릿 파일들이 
 * 올바른 위치에 존재하는지만 확인
 */
class TemplateExistenceTest {

    @Test
    @DisplayName("게시글 관련 템플릿 파일들이 존재하는지 확인")
    void articleTemplatesExist() {
        // Given & When & Then
        assertTemplateExists("templates/article/list.html");
        assertTemplateExists("templates/article/detail.html");
        assertTemplateExists("templates/article/form.html");
    }

    @Test
    @DisplayName("공통 템플릿 파일들이 존재하는지 확인")
    void commonTemplatesExist() {
        assertTemplateExists("templates/fragments/header.html");
        assertTemplateExists("templates/fragments/footer.html");
        assertTemplateExists("templates/fragments/aside.html");
        assertTemplateExists("templates/layouts/layout.html");
    }

    @Test
    @DisplayName("기본 페이지 템플릿이 존재하는지 확인")
    void basicTemplatesExist() {
        assertTemplateExists("templates/index.html");
        assertTemplateExists("templates/login.html");
        assertTemplateExists("templates/admin.html");
        assertTemplateExists("templates/manager.html");
    }

    /**
     * 템플릿 파일 존재 여부 확인 헬퍼 메서드
     */
    private void assertTemplateExists(String templatePath) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templatePath);
        assertNotNull(inputStream, "Template file should exist: " + templatePath);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            // Ignore close exception
        }
    }
}