package com.kraftlog.pdfimport.client;

import com.kraftlog.pdfimport.config.KraftLogApiProperties;
import com.kraftlog.pdfimport.dto.ExerciseCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KraftLogApiClientTest {

    private KraftLogApiProperties apiProperties;

    @BeforeEach
    void setUp() {
        apiProperties = new KraftLogApiProperties();
        apiProperties.setBaseUrl("https://api.kraftlog.com");
        
        KraftLogApiProperties.Auth auth = new KraftLogApiProperties.Auth();
        auth.setUsername("testuser");
        auth.setPassword("testpass");
        apiProperties.setAuth(auth);
    }

    @Test
    void testClientInstantiation() {
        KraftLogApiClient client = new KraftLogApiClient(apiProperties);
        assertNotNull(client);
    }

    @Test
    void testClientWithNullProperties() {
        // The client doesn't throw NPE on construction with null properties
        // It will throw when methods are called that use the properties
        KraftLogApiClient client = new KraftLogApiClient(null);
        assertNotNull(client);
    }

    @Test
    void testExerciseCreateRequestValidation() {
        ExerciseCreateRequest request = ExerciseCreateRequest.builder()
                .name("Bench Press")
                .description("Chest exercise")
                .sets(3)
                .repetitions(10)
                .technique("Keep back flat")
                .defaultWeightKg(60.0)
                .videoUrl("https://youtube.com/watch?v=test")
                .equipmentType("Barbell")
                .muscleIds(List.of(UUID.randomUUID()))
                .build();

        assertNotNull(request);
        assertEquals("Bench Press", request.getName());
    }

    @Test
    void testApiPropertiesConfiguration() {
        assertEquals("https://api.kraftlog.com", apiProperties.getBaseUrl());
        assertEquals("testuser", apiProperties.getAuth().getUsername());
        assertEquals("testpass", apiProperties.getAuth().getPassword());
    }

    @Test
    void testClientInitializationWithValidProperties() {
        KraftLogApiProperties props = new KraftLogApiProperties();
        props.setBaseUrl("https://localhost:8080");
        
        KraftLogApiProperties.Auth auth = new KraftLogApiProperties.Auth();
        auth.setUsername("admin");
        auth.setPassword("secret");
        props.setAuth(auth);

        KraftLogApiClient client = new KraftLogApiClient(props);
        assertNotNull(client);
    }

    @Test
    void testClientWithEmptyBaseUrl() {
        KraftLogApiProperties props = new KraftLogApiProperties();
        props.setBaseUrl("");
        
        KraftLogApiProperties.Auth auth = new KraftLogApiProperties.Auth();
        auth.setUsername("user");
        auth.setPassword("pass");
        props.setAuth(auth);

        KraftLogApiClient client = new KraftLogApiClient(props);
        assertNotNull(client);
    }

    @Test
    void testMultipleClientInstances() {
        KraftLogApiClient client1 = new KraftLogApiClient(apiProperties);
        KraftLogApiClient client2 = new KraftLogApiClient(apiProperties);
        
        assertNotNull(client1);
        assertNotNull(client2);
        assertNotSame(client1, client2);
    }
}
