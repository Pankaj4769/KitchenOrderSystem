package com.kos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kos.dto.Floor;
import com.kos.dto.MessageResponse;
import com.kos.service.FloorService;

@RestController
@RequestMapping("/api/floors")
public class FloorController {

    @Autowired
    FloorService floorService;

    // GET /api/floors/{restaurantId}
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<Floor>> getAllFloors(@PathVariable String restaurantId) {
        return new ResponseEntity<>(floorService.getAllFloors(restaurantId), HttpStatus.OK);
    }

    // POST /api/floors
    @PostMapping
    public ResponseEntity<Floor> addFloor(@RequestBody Floor floor) {
        return new ResponseEntity<>(floorService.addFloor(floor), HttpStatus.CREATED);
    }

    // DELETE /api/floors/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteFloor(@PathVariable Long id) {
        return new ResponseEntity<>(floorService.deleteFloor(id), HttpStatus.OK);
    }
}
