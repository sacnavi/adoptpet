package com.sacnavi.diplomado.adoptpet.svc.service.impl;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sacnavi.diplomado.adoptpet.svc.domain.Pet;
import com.sacnavi.diplomado.adoptpet.svc.domain.RegisterStatus;
import com.sacnavi.diplomado.adoptpet.svc.domain.Vaccine;
import com.sacnavi.diplomado.adoptpet.svc.dto.NewPetRequest;
import com.sacnavi.diplomado.adoptpet.svc.repository.PetRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.PetService;

/**
 * @author sacnavi
 */
@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private PetRepository petRepository;

    private static final Logger LOG = LoggerFactory
            .getLogger(PetServiceImpl.class);

    @Override
    public Pet addPet(NewPetRequest pet) {
        Pet newPet = Pet.builder().id(UUID.randomUUID().toString())
                .registerStatus(RegisterStatus.ARRIVED)
                .species(pet.getSpecies()).breed(pet.getBreed())
                .age(pet.getAge()).sex(pet.getSex()).spayed(pet.getSpayed())
                .foundIn(pet.getFoundIn()).comments(pet.getComments())
                .name(pet.getName()).photos(Collections.emptyList())
                .vaccines(Collections.emptyList()).build();

        return petRepository.insert(newPet);
    }

    @Override
    public Pet updatePet(String id, Pet pet) {
        Optional<Pet> existentPet = petRepository.findById(id);
        if (existentPet.isPresent()) {
            Pet newPet = existentPet.get();
            newPet.setSpecies(pet.getSpecies());
            newPet.setBreed(pet.getBreed());
            newPet.setAge(pet.getAge());
            newPet.setFoundIn(pet.getFoundIn());
            newPet.setComments(pet.getComments());
            newPet.setName(pet.getName());
            newPet.setPhotos(pet.getPhotos());
            newPet.setVaccines(pet.getVaccines());
            newPet.setRegisterStatus(pet.getRegisterStatus());
            newPet.setSex(pet.getSex());
            newPet.setSpayed(pet.getSpayed());
            return petRepository.save(newPet);
        }
        LOG.warn("Attempted to modify nonexistent pet:", id);
        return null;
    }

    @Override
    public Pet addPetPhoto(String id, String photoUrl) {
        Optional<Pet> existentPet = petRepository.findById(id);
        if (existentPet.isPresent()) {
            Pet pet = existentPet.get();
            pet.getPhotos().add(photoUrl);
            return petRepository.save(pet);
        }
        LOG.warn("Attempted to modify nonexistent pet:", id);
        return null;
    }

    @Override
    public Pet addPetVaccine(String id, Vaccine vaccine) {
        Optional<Pet> existentPet = petRepository.findById(id);
        if (existentPet.isPresent()) {
            Pet pet = existentPet.get();
            pet.getVaccines().add(vaccine);
            return petRepository.save(pet);
        }
        LOG.warn("Attempted to modify nonexistent pet:", id);
        return null;
    }

}
