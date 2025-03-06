package com.sacnavi.diplomado.adoptpet.svc.repository;

import com.sacnavi.diplomado.adoptpet.svc.domain.Pet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Optional;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
class PetRepositoryTest {

    @Autowired
    private PetRepository petRepository;

    private Pet testPet;

    @BeforeEach
    void setup() {
        testPet = new Pet();
        testPet.setId("1");
        testPet.setName("Test Pet");
        testPet.setBreed("Boxer");

        petRepository.save(testPet);
    }

    @Test
    void whenPetSaved_thenCanBeFound() {
        Optional<Pet> foundPet = petRepository.findById("1");
        assertEquals(foundPet, Optional.of(testPet));
        assertTrue(foundPet.isPresent());
        assertEquals("Test Pet", foundPet.get().getName());
        assertEquals("Boxer", foundPet.get().getBreed());
    }

    @Test
    void whenPetDeleted_thenCantBeFound() {
        petRepository.delete(testPet);
        Optional<Pet> foundPet = petRepository.findById("1");
        assertEquals(foundPet, Optional.empty());
        assertFalse(foundPet.isPresent());
    }
}