package com.sacnavi.diplomado.adoptpet.svc.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.sacnavi.diplomado.adoptpet.svc.domain.City;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author sacnavi
 */
@RequestMapping(path = "api/ciudades", produces = "application/json")
@CrossOrigin(origins = "*")
@Tag(name = "ciudad", description = "API de administración de ciudades")
public interface CityApi {

    @Operation(summary = "Listar todas las ciudades")
    @GetMapping()
    public List<City> listCities();

    @Operation(summary = "Crear ciudad")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public City addCity(@RequestBody City city);

    @Operation(summary = "Consulta ciudad por Id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            content = { @Content(mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = City.class)) }),
                    @ApiResponse(responseCode = "404",
                            description = "Ciudad no encontrada") })
    @GetMapping("{id}")
    public ResponseEntity<City> getCityById(@PathVariable String id);

    @Operation(summary = "Modificar ciudad por Id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            content = { @Content(mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = City.class)) }),
                    @ApiResponse(responseCode = "404",
                            description = "Ciudad inexistente") })
    @PutMapping(path = "{id}", consumes = "application/json")
    public ResponseEntity<City> updateCity(@PathVariable String id,
            @RequestBody City city);

    @Operation(summary = "Borrar ciudad por Id")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCity(@PathVariable String id);

}