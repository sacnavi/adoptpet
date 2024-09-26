package com.sacnavi.diplomado.adoptpet.svc.api.controller;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControllerHelper {

    static <T> ResponseEntity<T> handleUpdateResult(T updatedObject) {
        if (Objects.nonNull(updatedObject)) {
            return new ResponseEntity<>(updatedObject, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

}
