package com.sacnavi.diplomado.adoptpet.svc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sacnavi.diplomado.adoptpet.svc.api.controller.CityController;
import com.sacnavi.diplomado.adoptpet.svc.api.controller.PetController;
import com.sacnavi.diplomado.adoptpet.svc.repository.CityRepository;
import com.sacnavi.diplomado.adoptpet.svc.repository.PetRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.CityService;
import com.sacnavi.diplomado.adoptpet.svc.service.PetService;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
class AdoptpetApplicationTests {

    @Autowired
    private CityController cityController;

    @Autowired
    private PetController petController;

    @Autowired
    private CityService cityService;

    @Autowired
    private PetService petService;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CityRepository cityRepository;

    @Test
    void contextLoads() {
        assertThat(cityController).isNotNull();
        assertThat(petController).isNotNull();
        assertThat(cityService).isNotNull();
        assertThat(petService).isNotNull();
        assertThat(cityRepository).isNotNull();
        assertThat(petRepository).isNotNull();
    }
}
