package com.sacnavi.diplomado.adoptpet.svc.domain;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sacnavi
 */
@Data
@NoArgsConstructor
@Document(collection = "cities")
public class City {

    @Id
    private String id;
    private String code;
    private String name;
    private List<String> zips;

}
