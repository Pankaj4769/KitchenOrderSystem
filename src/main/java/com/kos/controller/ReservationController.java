package com.kos.controller;

import java.util.List;
import java.util.Map;

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

    @Autowired
    ReservationService reservationService;

    // GET /api/reservations/{restaurantId}
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<Reservation>> getAllReservations(@PathVariable String restaurantId) {
        return new ResponseEntity<>(reservationService.getAllReservations(restaurantId), HttpStatus.OK);
    }

    // GET /api/reservations/{restaurantId}/upcoming
    @GetMapping("/{restaurantId}/upcoming")
    public ResponseEntity<List<Reservation>> getUpcomingReservations(@PathVariable String restaurantId) {
        return new ResponseEntity<>(reservationService.getUpcomingReservations(restaurantId), HttpStatus.OK);
    }

    // POST /api/reservations
    @PostMapping
    public ResponseEntity<Reservation> addReservation(@RequestBody Reservation reservation) {
        return new ResponseEntity<>(reservationService.addReservation(reservation), HttpStatus.CREATED);
    }

    // PATCH /api/reservations/{id}/arrived
    @PatchMapping("/{id}/arrived")
    public ResponseEntity<Reservation> markArrived(@PathVariable Long id) {
        return new ResponseEntity<>(reservationService.markArrived(id), HttpStatus.OK);
    }

    // PATCH /api/reservations/{id}/status  { status: "CONFIRMED" | "NO_SHOW" | ... }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return new ResponseEntity<>(reservationService.updateStatus(id, body.get("status")), HttpStatus.OK);
    }

    // DELETE /api/reservations/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> cancelReservation(@PathVariable Long id) {
        return new ResponseEntity<>(reservationService.cancelReservation(id), HttpStatus.OK);
    }
}
