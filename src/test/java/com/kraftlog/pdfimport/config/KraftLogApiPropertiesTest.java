package com.kraftlog.pdfimport.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KraftLogApiPropertiesTest {

    @Test
    void testSettersAndGetters() {
        KraftLogApiProperties properties = new KraftLogApiProperties();
        
        properties.setBaseUrl("https://api.kraftlog.com");
        
        KraftLogApiProperties.Auth auth = new KraftLogApiProperties.Auth();
        auth.setUsername("testuser");
        auth.setPassword("testpass");
        properties.setAuth(auth);

        assertEquals("https://api.kraftlog.com", properties.getBaseUrl());
        assertNotNull(properties.getAuth());
        assertEquals("testuser", properties.getAuth().getUsername());
        assertEquals("testpass", properties.getAuth().getPassword());
    }

    @Test
    void testAuthInnerClass() {
        KraftLogApiProperties.Auth auth = new KraftLogApiProperties.Auth();
        
        auth.setUsername("admin");
        auth.setPassword("secret");

        assertEquals("admin", auth.getUsername());
        assertEquals("secret", auth.getPassword());
    }

    @Test
    void testDefaultValues() {
        KraftLogApiProperties properties = new KraftLogApiProperties();
        
        assertNull(properties.getBaseUrl());
        assertNull(properties.getAuth());
    }

    @Test
    void testAuthDefaultValues() {
        KraftLogApiProperties.Auth auth = new KraftLogApiProperties.Auth();
        
        assertNull(auth.getUsername());
        assertNull(auth.getPassword());
    }

    @Test
    void testCompleteConfiguration() {
        KraftLogApiProperties properties = new KraftLogApiProperties();
        properties.setBaseUrl("https://localhost:8080");
        
        KraftLogApiProperties.Auth auth = new KraftLogApiProperties.Auth();
        auth.setUsername("user123");
        auth.setPassword("pass123");
        properties.setAuth(auth);

        assertEquals("https://localhost:8080", properties.getBaseUrl());
        assertEquals("user123", properties.getAuth().getUsername());
        assertEquals("pass123", properties.getAuth().getPassword());
    }
}
