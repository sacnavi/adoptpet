package com.sacnavi.diplomado.adoptpet.svc.api.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.sacnavi.diplomado.adoptpet.svc.api.CityApi;
import com.sacnavi.diplomado.adoptpet.svc.domain.City;
import com.sacnavi.diplomado.adoptpet.svc.repository.CityRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.CityService;

/**
 * @author sacnavi
 */
@RestController
public class CityController implements CityApi {

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private CityService cityService;

    @Override
    public List<City> listCities() {
        return cityRepository.findAll();
    }

    @Override
    public City addCity(City city) {
        city.setId(UUID.randomUUID().toString());
        return cityRepository.insert(city);
    }

    @Override
    public ResponseEntity<City> getCityById(String id) {
        Optional<City> city = cityRepository.findById(id);
        if (city.isPresent()) {
            return new ResponseEntity<>(city.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<City> updateCity(String id, City city) {
        City updatedCity = cityService.updateCity(id, city);
        return ControllerHelper.handleUpdateResult(updatedCity);
    }

    @Override
    public void deleteCity(String id) {
        cityRepository.deleteById(id);
    }

}
