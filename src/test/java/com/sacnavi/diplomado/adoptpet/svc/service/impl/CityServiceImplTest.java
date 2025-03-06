package com.sacnavi.diplomado.adoptpet.svc.service.impl;

import com.sacnavi.diplomado.adoptpet.svc.domain.City;
import com.sacnavi.diplomado.adoptpet.svc.repository.CityRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.CityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CityServiceImplTest {

    @Autowired
    CityService cityService;

    @MockitoBean
    CityRepository cityRepository;

    @Test
    void updateExistentCity() {
        City testCity = new City();
        testCity.setId("testId");
        testCity.setName("Test City");
        testCity.setCode("TC");

        City existentCity = mock(City.class);

        when(cityRepository.findById(any())).thenReturn(Optional.of(existentCity));
        when(cityRepository.save(any())).thenReturn(testCity);
        City createdCity = cityService.updateCity("testId", testCity);

        verify(cityRepository, times(1)).findById(anyString());
        verify(cityRepository, times(1)).save(any());

        assertNotNull(createdCity);
        assertEquals("Test City", createdCity.getName());
        assertEquals("TC", createdCity.getCode());
        assertEquals("testId", createdCity.getId());
    }

    @Test
    void updateNonExistentCity() {
        City nonExistentCity = mock(City.class);

        when(cityRepository.findById(any())).thenReturn(Optional.empty());
        City createdCity = cityService.updateCity("testId", nonExistentCity);

        verify(cityRepository, times(1)).findById(anyString());
        verify(cityRepository, times(0)).save(any());
        assertNull(createdCity);
    }
}