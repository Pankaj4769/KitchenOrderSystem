package com.kos.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.MessageResponse;
import com.kos.dto.Reservation;
import com.kos.dto.Reservation.ReservationStatus;
import com.kos.repository.ReservationRepository;

@Service
public class ReservationService {

    Logger logger = LoggerFactory.getLogger(ReservationService.class);

    @Autowired
    ReservationRepository reservationRepository;

    // ── GET ALL ───────────────────────────────────────────────────────
    public List<Reservation> getAllReservations(String restaurantId) {
        return reservationRepository.findByRestaurantId(restaurantId);
    }

    // ── GET UPCOMING (today and future, status = UPCOMING / CONFIRMED / PENDING) ──
    public List<Reservation> getUpcomingReservations(String restaurantId) {
        return reservationRepository.findUpcoming(restaurantId, LocalDate.now());
    }

    // ── CREATE ────────────────────────────────────────────────────────
    public Reservation addReservation(Reservation reservation) {
        reservation.setStatus(ReservationStatus.UPCOMING);
        reservation.setCreatedAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    // ── MARK ARRIVED ──────────────────────────────────────────────────
    public Reservation markArrived(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reservation not found with id: " + id));
        reservation.setStatus(ReservationStatus.ARRIVED);
        return reservationRepository.save(reservation);
    }

    // ── UPDATE STATUS (confirm, no-show, etc.) ────────────────────────
    public Reservation updateStatus(Long id, String status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reservation not found with id: " + id));
        try {
            reservation.setStatus(ReservationStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
        return reservationRepository.save(reservation);
    }

    // ── CANCEL ────────────────────────────────────────────────────────
    public MessageResponse cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reservation not found with id: " + id));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        return new MessageResponse("Reservation cancelled successfully", true);
    }
}
