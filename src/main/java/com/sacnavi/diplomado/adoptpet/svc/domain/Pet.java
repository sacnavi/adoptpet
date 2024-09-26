package com.sacnavi.diplomado.adoptpet.svc.domain;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sacnavi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "pets")
public class Pet {

    @Id
    private String id;
    private String species;
    private String breed;
    private String name;
    private String comments;
    private Boolean spayed;
    private Integer age;
    private Location foundIn;
    private Sex sex;
    private RegisterStatus registerStatus;
    private List<Vaccine> vaccines;
    private List<String> photos;

}
