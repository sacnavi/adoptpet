package com.sacnavi.diplomado.adoptpet.svc.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ControllerHelperTest {

    @Test
    void handleUpdateResult() {
        ResponseEntity<Object> result = ControllerHelper.handleUpdateResult(new Object());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void handleUpdateNullResult() {
        ResponseEntity<Object> result = ControllerHelper.handleUpdateResult(null);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }
}