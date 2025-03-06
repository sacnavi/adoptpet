package com.sacnavi.diplomado.adoptpet.svc.service.impl;

import com.sacnavi.diplomado.adoptpet.svc.domain.Pet;
import com.sacnavi.diplomado.adoptpet.svc.domain.Vaccine;
import com.sacnavi.diplomado.adoptpet.svc.dto.NewPetRequest;
import com.sacnavi.diplomado.adoptpet.svc.repository.PetRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class PetServiceImplTest {

    @Autowired
    PetService petService;

    @MockitoBean
    PetRepository petRepository;

    @Test
    void addPet() {
        NewPetRequest petRequest = mock(NewPetRequest.class);
        Pet mockPet = mock(Pet.class);

        when(petRepository.insert(any(Pet.class))).thenReturn(mockPet);
        Pet newPet = petService.addPet(petRequest);
        verify(petRepository, times(1)).insert(any(Pet.class));
        assertNotNull(newPet);
    }

    @Test
    void updateExistentPet() {
        Pet testPet = new Pet();
        testPet.setId("testId");
        testPet.setName("Test Pet");
        testPet.setBreed("Test Breed");

        Pet existentPet = mock(Pet.class);

        when(petRepository.findById(anyString())).thenReturn(Optional.of(existentPet));
        when(petRepository.save(any())).thenReturn(testPet);
        Pet createdPet = petService.updatePet("testId", testPet);

        verify(petRepository, times(1)).findById(anyString());
        verify(petRepository, times(1)).save(any());
        assertNotNull(createdPet);
        assertEquals("Test Pet", createdPet.getName());
        assertEquals("Test Breed", createdPet.getBreed());
        assertEquals("testId", createdPet.getId());
    }

    @Test
    void updateNonExistentPet() {
        Pet nonExistentPet = mock(Pet.class);

        when(petRepository.findById(anyString())).thenReturn(Optional.empty());
        Pet updatedPet = petService.updatePet("testId", nonExistentPet);

        verify(petRepository, times(1)).findById(anyString());
        verify(petRepository, times(0)).save(any());
        assertNull(updatedPet);
    }

    @Test
    void addPetPhoto() {
        Pet existentPet = mock(Pet.class);

        when(petRepository.findById(anyString())).thenReturn(Optional.of(existentPet));
        when(petRepository.save(any())).thenReturn(existentPet);
        Pet updatedPet = petService.addPetPhoto("testId", "testPhotoUrl");

        verify(petRepository, times(1)).findById(anyString());
        verify(petRepository, times(1)).save(any());
        assertNotNull(updatedPet);
    }

    @Test
    void addPhotoNonExistentPet() {
        when(petRepository.findById(anyString())).thenReturn(Optional.empty());
        Pet updatedPet = petService.addPetPhoto("testId", "testPhotoUrl");

        verify(petRepository, times(1)).findById(anyString());
        verify(petRepository, times(0)).save(any());
        assertNull(updatedPet);
    }

    @Test
    void addPetVaccine() {
        Pet existentPet = mock(Pet.class);

        when(petRepository.findById(anyString())).thenReturn(Optional.of(existentPet));
        when(petRepository.save(any())).thenReturn(existentPet);
        Pet updatedPet = petService.addPetVaccine("testId", mock(Vaccine.class));

        verify(petRepository, times(1)).findById(anyString());
        verify(petRepository, times(1)).save(any());
        assertNotNull(updatedPet);
    }

    @Test
    void addVaccineNonExistentPet() {
        when(petRepository.findById(anyString())).thenReturn(Optional.empty());
        Pet updatedPet = petService.addPetVaccine("testId", mock(Vaccine.class));

        verify(petRepository, times(1)).findById(anyString());
        verify(petRepository, times(0)).save(any());
        assertNull(updatedPet);
    }
}