package com.genius.primavera.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.genius.primavera.domain.User;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Slf4j
@SpringBootTest
@EnableTestContainers
@ActiveProfiles("test")
@DisplayName("PrimaveraService 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraServiceTest {

    private MockRestServiceServer mockServer;
    private PrimaveraService primaveraService;
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplateForMock = restTemplateBuilder.build();
        mockServer = MockRestServiceServer.createServer(restTemplateForMock);
        primaveraService = new PrimaveraService(new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplateForMock;
            }

            @Override
            public RestTemplateBuilder messageConverters(org.springframework.http.converter.HttpMessageConverter<?>... messageConverters) {
                return this;
            }
        });
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @Order(1)
    @DisplayName("사용자 조회 성공 테스트")
    public void getUserTest() throws Exception {
        long userId = 1L;
        User expectedUser = User.builder().id(userId).email("genius@primavera.com").nickname("Genius").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        String userJson = objectMapper.writeValueAsString(expectedUser);
        mockServer.expect(ExpectedCount.once(), requestTo("http://localhost:8080/users/" + userId)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(userJson, MediaType.APPLICATION_JSON));
        User actualUser = primaveraService.getUser(userId);
        assertNotNull(actualUser);
        assertEquals(expectedUser.getId(), actualUser.getId());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getNickname(), actualUser.getNickname());
        mockServer.verify();
    }

    @Test
    @Order(2)
    @DisplayName("사용자 조회 실패 테스트 (404 Not Found)")
    public void getUserNotFoundTest() {
        long userId = 999L;
        mockServer.expect(ExpectedCount.once(), requestTo("http://localhost:8080/users/" + userId)).andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatusCode.valueOf(404)).contentType(MediaType.APPLICATION_JSON));
        assertThrows(HttpClientErrorException.class, () -> primaveraService.getUser(userId));
        mockServer.verify();
    }

    @Test
    @Order(3)
    @DisplayName("네트워크 연결 실패 테스트 (ResourceAccessException)")
    public void getNetworkErrorTest() {
        long userId = 100L;
        mockServer.expect(ExpectedCount.once(), requestTo("http://localhost:8080/users/" + userId)).andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new ResourceAccessException("Simulated connection refused");
                });
        assertThrows(ResourceAccessException.class, () -> primaveraService.getUser(userId));
        mockServer.verify();
    }
}