package com.sacnavi.diplomado.adoptpet.svc.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sacnavi.diplomado.adoptpet.svc.domain.City;
import com.sacnavi.diplomado.adoptpet.svc.repository.CityRepository;
import com.sacnavi.diplomado.adoptpet.svc.service.CityService;

/**
 * @author sacnavi
 */
@Service
public class CityServiceImpl implements CityService {

    @Autowired
    private CityRepository cityRepository;

    private static final Logger LOG = LoggerFactory
            .getLogger(CityServiceImpl.class);

    @Override
    public City updateCity(String id, City city) {
        Optional<City> existentCity = cityRepository.findById(id);
        if (existentCity.isPresent()) {
            City newCity = existentCity.get();
            newCity.setCode(city.getCode());
            newCity.setName(city.getName());
            newCity.setZips(city.getZips());
            return cityRepository.save(newCity);
        }
        LOG.warn("Attempted to modify nonexistent city:", id);
        return null;
    }

}
