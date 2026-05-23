package com.kos.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(FloorController.class);

    @Autowired
    FloorService floorService;

    // GET /api/floors/{restaurantId}
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<Floor>> getAllFloors(@PathVariable String restaurantId) {
        logger.info("Entering getAllFloors() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<List<Floor>> result = new ResponseEntity<>(floorService.getAllFloors(restaurantId), HttpStatus.OK);
            logger.info("Exiting getAllFloors()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getAllFloors(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // POST /api/floors
    @PostMapping
    public ResponseEntity<Floor> addFloor(@RequestBody Floor floor) {
        logger.info("Entering addFloor()");
        try {
            ResponseEntity<Floor> result = new ResponseEntity<>(floorService.addFloor(floor), HttpStatus.CREATED);
            logger.info("Exiting addFloor()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in addFloor(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // DELETE /api/floors/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteFloor(@PathVariable Long id) {
        logger.info("Entering deleteFloor() with id={}", id);
        try {
            ResponseEntity<MessageResponse> result = new ResponseEntity<>(floorService.deleteFloor(id), HttpStatus.OK);
            logger.info("Exiting deleteFloor()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in deleteFloor(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
