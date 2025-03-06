package com.sacnavi.diplomado.adoptpet.svc.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sacnavi.diplomado.adoptpet.svc.domain.City;
import com.sacnavi.diplomado.adoptpet.svc.repository.CityRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.CityService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CityControllerTest {

    private static final String BASE_PATH = "/api/ciudades";
    private static final String TEST_ID = "testId";
    private static final String ID_PATH = BASE_PATH.concat("/{id}");
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    private CityService cityService;
    @MockitoBean
    private CityRepository cityRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listCities() throws Exception {
        City testCity = new City();
        testCity.setId("1");
        testCity.setName("Test City");
        testCity.setCode("TC");
        testCity.setZips(new ArrayList<>(0));

        when(cityRepository.findAll()).thenReturn(Collections.singletonList(testCity));
        MvcResult result = mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<City> cities = objectMapper.readValue(content, new TypeReference<>() {
        });
        City resultCity = cities.get(0);

        verify(cityRepository, times(1)).findAll();
        assertNotNull(resultCity);
        assertEquals(testCity.getId(), resultCity.getId());
        assertEquals(testCity.getCode(), resultCity.getCode());
        assertEquals(testCity.getName(), resultCity.getName());
        assertEquals(testCity.getZips(), resultCity.getZips());
    }

    @Test
    void listCitiesEmpty() throws Exception {
        when(cityRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[]")));
    }

    @Test
    void addCity() throws Exception {
        when(cityRepository.insert(any(City.class))).thenReturn(mock(City.class));

        ResultActions result = mockMvc.perform(
                        MockMvcRequestBuilders.post(BASE_PATH)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mock(City.class))))
                .andExpect(status().isCreated());
        String content = result.andReturn().getResponse().getContentAsString();
        City createdCity = objectMapper.readValue(content, City.class);

        verify(cityRepository, times(1)).insert(any(City.class));
        assertNotNull(createdCity);
    }

    @Test
    void getCityById() throws Exception {
        when(cityRepository.findById(TEST_ID)).thenReturn(Optional.of(mock(City.class)));
        MvcResult result = mockMvc.perform(get(ID_PATH, TEST_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        City foundCity = objectMapper.readValue(content, City.class);

        verify(cityRepository, times(1)).findById(TEST_ID);
        assertNotNull(foundCity);
    }

    @Test
    void getCityByIdNotFound() throws Exception {
        when(cityRepository.findById(TEST_ID)).thenReturn(Optional.empty());
        MvcResult result = mockMvc.perform(get(ID_PATH, TEST_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        verify(cityRepository, times(1)).findById(TEST_ID);
        assertEquals(StringUtils.EMPTY, content);
    }

    @Test
    void updateCity() throws Exception {
        when(cityService.updateCity(anyString(), any(City.class))).thenReturn(mock(City.class));

        ResultActions result = mockMvc.perform(
                        MockMvcRequestBuilders.put(ID_PATH, TEST_ID)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mock(City.class))))
                .andExpect(status().isOk());
        String content = result.andReturn().getResponse().getContentAsString();
        City createdCity = objectMapper.readValue(content, City.class);

        verify(cityService, times(1)).updateCity(anyString(), any(City.class));
        assertNotNull(createdCity);
    }

    @Test
    void deleteCity() throws Exception {
        doNothing().when(cityRepository).deleteById(anyString());
        mockMvc.perform(delete(ID_PATH, TEST_ID));

        verify(cityRepository, times(1)).deleteById(TEST_ID);
    }
}