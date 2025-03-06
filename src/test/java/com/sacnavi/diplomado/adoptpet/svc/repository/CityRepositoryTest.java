package com.sacnavi.diplomado.adoptpet.svc.repository;

import com.sacnavi.diplomado.adoptpet.svc.domain.City;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.ArrayList;
import java.util.Optional;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
class CityRepositoryTest {

    @Autowired
    private CityRepository cityRepository;

    private City testCity;

    @BeforeEach
    void setup() {
        testCity = new City();
        testCity.setId("1");
        testCity.setName("Test City");
        testCity.setCode("TC");
        testCity.setZips(new ArrayList<>(0));
        cityRepository.save(testCity);
    }

    @Test
    void whenCitySaved_thenCanBeFound() {
        Optional<City> foundCity = cityRepository.findById("1");
        assertEquals(foundCity, Optional.of(testCity));
        assertTrue(foundCity.isPresent());
        assertEquals("TC", foundCity.get().getCode());
        assertEquals("Test City", foundCity.get().getName());
    }

    @Test
    void whenCityDeleted_thenCantBeFound() {
        cityRepository.delete(testCity);
        Optional<City> foundCity = cityRepository.findById("1");
        assertEquals(foundCity, Optional.empty());
        assertFalse(foundCity.isPresent());
    }
}