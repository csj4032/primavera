package com.genius.primavera;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HelloJsonTest {

    @Test
    @DisplayName("JSON 테스트")
    public void jsonTest() {
        Assertions.assertThat(true).isTrue();
    }
}
