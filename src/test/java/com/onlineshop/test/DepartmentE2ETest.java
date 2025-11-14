package com.onlineshop.test;

import com.onlineshop.test.repository.DepartmentRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8081")
class DepartmentE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    DepartmentRepository departmentRepository;

    @BeforeAll
    static void setUp() {
        RestAssured.port = 8081;
    }

    @Test
    void createDepartment_shouldReturn201_andPersist() {
        var payloadRequest = """
                { "name": "IT", "location": "Tashkent" }
                """;

        var id =
                RestAssured.given()
                        .contentType(JSON)
                        .body(payloadRequest)
                        .when()
                        .post("/api/departments")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("id");

        assertThat(departmentRepository.findById(((Number)id).longValue())).isPresent()
                .get().extracting("name").isEqualTo("IT");
    }
}
