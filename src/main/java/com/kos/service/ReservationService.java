package com.kos.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(ReservationService.class);

    @Autowired
    ReservationRepository reservationRepository;

    // ── GET ALL ───────────────────────────────────────────────────────
    public List<Reservation> getAllReservations(String restaurantId) {
        logger.info("Entering getAllReservations() with restaurantId={}", restaurantId);
        try {
            List<Reservation> result = reservationRepository.findByRestaurantId(restaurantId);
            logger.info("Exiting getAllReservations()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getAllReservations(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── GET UPCOMING (today and future, status = UPCOMING / CONFIRMED / PENDING) ──
    public List<Reservation> getUpcomingReservations(String restaurantId) {
        logger.info("Entering getUpcomingReservations() with restaurantId={}", restaurantId);
        try {
            List<Reservation> result = reservationRepository.findUpcoming(restaurantId, LocalDate.now());
            logger.info("Exiting getUpcomingReservations()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getUpcomingReservations(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── CREATE ────────────────────────────────────────────────────────
    public Reservation addReservation(Reservation reservation) {
        logger.info("Entering addReservation()");
        try {
            reservation.setStatus(ReservationStatus.UPCOMING);
            reservation.setCreatedAt(LocalDateTime.now());
            Reservation result = reservationRepository.save(reservation);
            logger.info("Exiting addReservation()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in addReservation(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── MARK ARRIVED ──────────────────────────────────────────────────
    public Reservation markArrived(Long id) {
        logger.info("Entering markArrived() with id={}", id);
        try {
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Reservation not found with id: " + id));
            reservation.setStatus(ReservationStatus.ARRIVED);
            Reservation result = reservationRepository.save(reservation);
            logger.info("Exiting markArrived()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in markArrived(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── UPDATE STATUS (confirm, no-show, etc.) ────────────────────────
    public Reservation updateStatus(Long id, String status) {
        logger.info("Entering updateStatus() with id={}", id);
        try {
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Reservation not found with id: " + id));
            try {
                reservation.setStatus(ReservationStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
            }
            Reservation result = reservationRepository.save(reservation);
            logger.info("Exiting updateStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updateStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── CANCEL ────────────────────────────────────────────────────────
    public MessageResponse cancelReservation(Long id) {
        logger.info("Entering cancelReservation() with id={}", id);
        try {
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Reservation not found with id: " + id));
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            MessageResponse result = new MessageResponse("Reservation cancelled successfully", true);
            logger.info("Exiting cancelReservation()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in cancelReservation(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
