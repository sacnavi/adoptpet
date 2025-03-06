package com.sacnavi.diplomado.adoptpet.svc.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sacnavi.diplomado.adoptpet.svc.domain.Pet;
import com.sacnavi.diplomado.adoptpet.svc.domain.Vaccine;
import com.sacnavi.diplomado.adoptpet.svc.dto.NewPetRequest;
import com.sacnavi.diplomado.adoptpet.svc.repository.PetRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.PetService;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PetControllerTest {

    private static final String BASE_PATH = "/api/mascotas";
    private static final String TEST_ID = "testId";
    private static final String ID_PATH = BASE_PATH.concat("/{id}");
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    private PetService petService;
    @MockitoBean
    private PetRepository petRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listPets() throws Exception {
        Pet testPet = new Pet();
        testPet.setId(TEST_ID);
        testPet.setBreed("Test Breed");
        testPet.setName("Test Pet");

        when(petRepository.findAll()).thenReturn(Collections.singletonList(testPet));
        MvcResult result = mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<Pet> pets = objectMapper.readValue(content, new TypeReference<>() {
        });
        Pet resultPet = pets.get(0);

        verify(petRepository, times(1)).findAll();
        assertNotNull(resultPet);
        assertEquals(testPet.getId(), resultPet.getId());
        assertEquals(testPet.getBreed(), resultPet.getBreed());
        assertEquals(testPet.getName(), resultPet.getName());
    }

    @Test
    void listPetsEmpty() throws Exception {
        when(petRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[]")));
    }

    @Test
    void addPet() throws Exception {
        when(petService.addPet(any(NewPetRequest.class))).thenReturn(mock(Pet.class));

        ResultActions result = mockMvc.perform(
                        MockMvcRequestBuilders.post(BASE_PATH)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mock(NewPetRequest.class))))
                .andExpect(status().isCreated());
        String content = result.andReturn().getResponse().getContentAsString();
        Pet createdPet = objectMapper.readValue(content, Pet.class);

        verify(petService, times(1)).addPet(any(NewPetRequest.class));
        assertNotNull(createdPet);
    }

    @Test
    void getPetById() throws Exception {
        when(petRepository.findById(TEST_ID)).thenReturn(Optional.of(mock(Pet.class)));
        MvcResult result = mockMvc.perform(get(ID_PATH, TEST_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Pet foundPet = objectMapper.readValue(content, Pet.class);

        verify(petRepository, times(1)).findById(TEST_ID);
        assertNotNull(foundPet);
    }

    @Test
    void getPetByIdNotFound() throws Exception {
        when(petRepository.findById(TEST_ID)).thenReturn(Optional.empty());
        MvcResult result = mockMvc.perform(get(ID_PATH, TEST_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        verify(petRepository, times(1)).findById(TEST_ID);
        assertEquals(StringUtils.EMPTY, content);
    }

    @Test
    void updatePet() throws Exception {
        when(petService.updatePet(anyString(), any(Pet.class))).thenReturn(mock(Pet.class));

        ResultActions result = mockMvc.perform(
                        MockMvcRequestBuilders.put(ID_PATH, TEST_ID)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mock(Pet.class))))
                .andExpect(status().isOk());
        String content = result.andReturn().getResponse().getContentAsString();
        Pet createdPet = objectMapper.readValue(content, Pet.class);

        verify(petService, times(1)).updatePet(anyString(), any(Pet.class));
        assertNotNull(createdPet);
    }

    @Test
    void addPetPhoto() throws Exception {
        when(petService.addPetPhoto(anyString(), anyString())).thenReturn(mock(Pet.class));

        ResultActions result = mockMvc.perform(
                        MockMvcRequestBuilders.put(ID_PATH.concat("/photo"), TEST_ID)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("testPhotoUrl"))
                .andExpect(status().isOk());
        String content = result.andReturn().getResponse().getContentAsString();
        Pet createdPet = objectMapper.readValue(content, Pet.class);

        verify(petService, times(1)).addPetPhoto(anyString(), anyString());
        assertNotNull(createdPet);
    }

    @Test
    void addPetVaccine() throws Exception {
        when(petService.addPetVaccine(anyString(), any(Vaccine.class))).thenReturn(mock(Pet.class));

        ResultActions result = mockMvc.perform(
                        MockMvcRequestBuilders.put(ID_PATH.concat("/vaccine"), TEST_ID)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mock(Vaccine.class))))
                .andExpect(status().isOk());
        String content = result.andReturn().getResponse().getContentAsString();
        Pet createdPet = objectMapper.readValue(content, Pet.class);

        verify(petService, times(1)).addPetVaccine(anyString(), any(Vaccine.class));
        assertNotNull(createdPet);
    }

    @Test
    void deletePet() throws Exception {
        doNothing().when(petRepository).deleteById(anyString());
        mockMvc.perform(delete(ID_PATH, TEST_ID));

        verify(petRepository, times(1)).deleteById(TEST_ID);
    }
}