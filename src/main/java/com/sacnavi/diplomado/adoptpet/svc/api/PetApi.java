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

import com.sacnavi.diplomado.adoptpet.svc.domain.Pet;
import com.sacnavi.diplomado.adoptpet.svc.domain.Vaccine;
import com.sacnavi.diplomado.adoptpet.svc.dto.NewPetRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author sacnavi
 */

@RequestMapping(path = "api/mascotas", produces = "application/json")
@CrossOrigin(origins = "*")
@Tag(name = "mascota", description = "API de administración de mascotas")
public interface PetApi {

    @Operation(summary = "Listar todas las mascotas")
    @GetMapping()
    public List<Pet> listPets();

    @Operation(summary = "Crear mascota")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Pet addPet(@RequestBody NewPetRequest pet);

    @Operation(summary = "Consulta mascota por Id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            content = { @Content(mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = Pet.class)) }),
                    @ApiResponse(responseCode = "404",
                            description = "Mascota no encontrada") })
    @GetMapping("{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable String id);

    @Operation(summary = "Modificar mascota por Id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            content = { @Content(mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = Pet.class)) }),
                    @ApiResponse(responseCode = "404",
                            description = "Mascota inexistente") })
    @PutMapping(path = "{id}", consumes = "application/json")
    public ResponseEntity<Pet> updatePet(@PathVariable String id,
            @RequestBody Pet pet);

    @Operation(summary = "Agregar foto a una mascota")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            content = { @Content(mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = Pet.class)) }),
                    @ApiResponse(responseCode = "404",
                            description = "Mascota inexistente") })
    @PutMapping(path = "{id}/photo", consumes = "application/json")
    public ResponseEntity<Pet> addPetPhoto(@PathVariable String id,
            @RequestBody String photoUrl);

    @Operation(summary = "Agregar vacuna a una mascota")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            content = { @Content(mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = Pet.class)) }),
                    @ApiResponse(responseCode = "404",
                            description = "Mascota inexistente") })
    @PutMapping(path = "{id}/vaccine", consumes = "application/json")
    public ResponseEntity<Pet> addPetVaccine(@PathVariable String id,
            @RequestBody Vaccine vaccine);

    @Operation(summary = "Borrar mascota por Id")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePet(@PathVariable String id);

}
