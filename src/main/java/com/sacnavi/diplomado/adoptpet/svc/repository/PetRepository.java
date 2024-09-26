package com.sacnavi.diplomado.adoptpet.svc.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sacnavi.diplomado.adoptpet.svc.domain.Pet;

/**
 * @author sacnavi
 */
@Repository
public interface PetRepository extends MongoRepository<Pet, String> {

}
