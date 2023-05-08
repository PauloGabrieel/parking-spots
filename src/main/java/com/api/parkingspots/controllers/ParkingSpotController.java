package com.api.parkingspots.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
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
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingspots.dtos.ParkingSpotDto;
import com.api.parkingspots.models.ParkingSpotModel;
import com.api.parkingspots.services.ParkingSpotService;

import jakarta.validation.Valid;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto ) {
        if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
        }

        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking spot number is already in use!");       
        }

        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Already a parking spot to same block and apartment");
        }

        ParkingSpotModel parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }

    @GetMapping
    public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpot() {
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> removeParkingSpotById(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingspotModelOptional = parkingSpotService.findById(id);
        if(!parkingspotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad_request: this id doesn't exists");
        }
        parkingSpotService.deleteParkingSpot(parkingspotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking spot deteled successfully.");
        
    }
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto) {
        Optional<ParkingSpotModel> parkingspotModelOptional = parkingSpotService.findById(id);
        if(!parkingspotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad_request: this id doesn't exists");
        }
        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking spot number is already in use!");       
        }
        var parkingspotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingspotModel);
        parkingspotModel.setId(parkingspotModelOptional.get().getId());
        parkingspotModel.setRegistrationDate(parkingspotModelOptional.get().getRegistrationDate());
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingspotModel));
    }
}
