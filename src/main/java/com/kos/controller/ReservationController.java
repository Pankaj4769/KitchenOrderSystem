package com.kos.controller;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kos.dto.MessageResponse;
import com.kos.dto.Reservation;
import com.kos.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final Logger logger = LogManager.getLogger(ReservationController.class);

    @Autowired
    ReservationService reservationService;

    // GET /api/reservations/{restaurantId}
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<Reservation>> getAllReservations(@PathVariable String restaurantId) {
        logger.info("Entering getAllReservations() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<List<Reservation>> result = new ResponseEntity<>(reservationService.getAllReservations(restaurantId), HttpStatus.OK);
            logger.info("Exiting getAllReservations()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getAllReservations(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // GET /api/reservations/{restaurantId}/upcoming
    @GetMapping("/{restaurantId}/upcoming")
    public ResponseEntity<List<Reservation>> getUpcomingReservations(@PathVariable String restaurantId) {
        logger.info("Entering getUpcomingReservations() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<List<Reservation>> result = new ResponseEntity<>(reservationService.getUpcomingReservations(restaurantId), HttpStatus.OK);
            logger.info("Exiting getUpcomingReservations()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getUpcomingReservations(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // POST /api/reservations
    @PostMapping
    public ResponseEntity<Reservation> addReservation(@RequestBody Reservation reservation) {
        logger.info("Entering addReservation()");
        try {
            ResponseEntity<Reservation> result = new ResponseEntity<>(reservationService.addReservation(reservation), HttpStatus.CREATED);
            logger.info("Exiting addReservation()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in addReservation(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // PATCH /api/reservations/{id}/arrived
    @PatchMapping("/{id}/arrived")
    public ResponseEntity<Reservation> markArrived(@PathVariable Long id) {
        logger.info("Entering markArrived() with id={}", id);
        try {
            ResponseEntity<Reservation> result = new ResponseEntity<>(reservationService.markArrived(id), HttpStatus.OK);
            logger.info("Exiting markArrived()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in markArrived(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // PATCH /api/reservations/{id}/status  { status: "CONFIRMED" | "NO_SHOW" | ... }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        logger.info("Entering updateStatus() with id={}", id);
        try {
            ResponseEntity<Reservation> result = new ResponseEntity<>(reservationService.updateStatus(id, body.get("status")), HttpStatus.OK);
            logger.info("Exiting updateStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updateStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // DELETE /api/reservations/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> cancelReservation(@PathVariable Long id) {
        logger.info("Entering cancelReservation() with id={}", id);
        try {
            ResponseEntity<MessageResponse> result = new ResponseEntity<>(reservationService.cancelReservation(id), HttpStatus.OK);
            logger.info("Exiting cancelReservation()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in cancelReservation(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
