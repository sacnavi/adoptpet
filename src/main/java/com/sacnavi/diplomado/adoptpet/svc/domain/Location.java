package com.sacnavi.diplomado.adoptpet.svc.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sacnavi
 */
@Data
@NoArgsConstructor
public class Location {

    private String cityName;
    private String zip;
    private String comments;
}
