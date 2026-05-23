package com.kos.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.Floor;
import com.kos.dto.MessageResponse;
import com.kos.repository.FloorRepository;

@Service
public class FloorService {

    private static final Logger logger = LogManager.getLogger(FloorService.class);

    @Autowired
    FloorRepository floorRepository;

    public List<Floor> getAllFloors(String restaurantId) {
        logger.info("Entering getAllFloors() with restaurantId={}", restaurantId);
        try {
            List<Floor> result = floorRepository.findByRestaurantId(restaurantId);
            logger.info("Exiting getAllFloors()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getAllFloors(): {}", e.getMessage(), e);
            throw e;
        }
    }

    public Floor addFloor(Floor floor) {
        logger.info("Entering addFloor()");
        try {
            Floor result = floorRepository.save(floor);
            logger.info("Exiting addFloor()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in addFloor(): {}", e.getMessage(), e);
            throw e;
        }
    }

    public MessageResponse deleteFloor(Long id) {
        logger.info("Entering deleteFloor() with id={}", id);
        try {
            Floor floor = floorRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Floor not found with id: " + id));
            floorRepository.delete(floor);
            MessageResponse result = new MessageResponse("Floor deleted successfully", true);
            logger.info("Exiting deleteFloor()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in deleteFloor(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
