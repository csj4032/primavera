package com.genius.primavera.interfaces;

import com.genius.primavera.application.plant.IPlantService;
import com.genius.primavera.domain.Plant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlantController {

	private final IPlantService plantService;

	@GetMapping("/plants")
	public ResponseEntity<List<Plant>> searchPlants(@RequestParam(value = "searchTerm", required = false, defaultValue = "None") String searchTerm) {
		try {
			List<Plant> plants = plantService.fetchPlants(searchTerm);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<>(plants, headers, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
