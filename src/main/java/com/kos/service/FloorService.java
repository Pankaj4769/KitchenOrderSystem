package com.kos.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.Floor;
import com.kos.dto.MessageResponse;
import com.kos.repository.FloorRepository;

@Service
public class FloorService {

    Logger logger = LoggerFactory.getLogger(FloorService.class);

    @Autowired
    FloorRepository floorRepository;

    public List<Floor> getAllFloors(String restaurantId) {
        return floorRepository.findByRestaurantId(restaurantId);
    }

    public Floor addFloor(Floor floor) {
        return floorRepository.save(floor);
    }

    public MessageResponse deleteFloor(Long id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Floor not found with id: " + id));
        floorRepository.delete(floor);
        return new MessageResponse("Floor deleted successfully", true);
    }
}
