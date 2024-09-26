package com.sacnavi.diplomado.adoptpet.svc.api.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.sacnavi.diplomado.adoptpet.svc.api.PetApi;
import com.sacnavi.diplomado.adoptpet.svc.domain.Pet;
import com.sacnavi.diplomado.adoptpet.svc.domain.Vaccine;
import com.sacnavi.diplomado.adoptpet.svc.dto.NewPetRequest;
import com.sacnavi.diplomado.adoptpet.svc.repository.PetRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.PetService;

/**
 * @author sacnavi
 */
@RestController
public class PetController implements PetApi {

    @Autowired
    private PetRepository petRepository;
    @Autowired
    private PetService petService;

    @Override
    public List<Pet> listPets() {
        return petRepository.findAll();
    }

    @Override
    public Pet addPet(NewPetRequest pet) {
        return petService.addPet(pet);
    }

    @Override
    public ResponseEntity<Pet> getPetById(String id) {
        Optional<Pet> pet = petRepository.findById(id);
        if (pet.isPresent()) {
            return new ResponseEntity<>(pet.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Pet> updatePet(String id, Pet pet) {
        Pet updatedPet = petService.updatePet(id, pet);
        return ControllerHelper.handleUpdateResult(updatedPet);

    }

    @Override
    public ResponseEntity<Pet> addPetPhoto(String id, String photoUrl) {
        Pet updatedPet = petService.addPetPhoto(id, photoUrl);
        return ControllerHelper.handleUpdateResult(updatedPet);
    }

    @Override
    public ResponseEntity<Pet> addPetVaccine(String id, Vaccine vaccine) {
        Pet updatedPet = petService.addPetVaccine(id, vaccine);
        return ControllerHelper.handleUpdateResult(updatedPet);
    }

    @Override
    public void deletePet(String id) {
        petRepository.deleteById(id);

    }

}
