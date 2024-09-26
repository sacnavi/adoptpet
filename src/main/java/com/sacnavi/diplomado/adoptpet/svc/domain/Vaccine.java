package com.sacnavi.diplomado.adoptpet.svc.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sacnavi
 */
@Data
@NoArgsConstructor
public class Vaccine {

    private String type;
    private String lot;
    private LocalDate applicationDate;

}
