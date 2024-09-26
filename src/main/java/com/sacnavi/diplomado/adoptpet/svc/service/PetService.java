package com.sacnavi.diplomado.adoptpet.svc.service;

import com.sacnavi.diplomado.adoptpet.svc.domain.Pet;
import com.sacnavi.diplomado.adoptpet.svc.domain.Vaccine;
import com.sacnavi.diplomado.adoptpet.svc.dto.NewPetRequest;

/**
 * @author sacnavi
 */
public interface PetService {

    Pet addPet(NewPetRequest pet);

    Pet updatePet(String id, Pet pet);

    Pet addPetPhoto(String id, String photoUrl);

    Pet addPetVaccine(String id, Vaccine vaccine);

}
