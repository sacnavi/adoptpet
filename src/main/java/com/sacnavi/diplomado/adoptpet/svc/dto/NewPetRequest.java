package com.sacnavi.diplomado.adoptpet.svc.dto;

import com.sacnavi.diplomado.adoptpet.svc.domain.Location;
import com.sacnavi.diplomado.adoptpet.svc.domain.Sex;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sacnavi
 */
@Data
@NoArgsConstructor
public class NewPetRequest {

    private String species;
    private String breed;
    private String name;
    private String comments;
    private Boolean spayed;
    private Integer age;
    private Sex sex;
    private Location foundIn;

}
