package ng.sciCloud.library.service;

import ng.sciCloud.library.model.*;
import ng.sciCloud.library.repository.ReservationRepository;
import ng.sciCloud.library.utils.validators.ReservationValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationValidation validation;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationValidation validation) {
        this.reservationRepository = reservationRepository;
        this.validation = validation;
    }

    @Transactional
    public Reservation reserve(Long userId, Long bookId) {
        AppUser user = validation.requireUser(userId);
        Book book = validation.requireBookForUpdate(bookId);
        validation.validateReserve(userId, bookId, book);

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancel(Long reservationId, Long userId) {
        Reservation reservation = validation.requireReservation(reservationId);
        validation.validateCancel(reservation, userId);
        reservation.setStatus(ReservationStatus.CANCELLED);
    }

    public List<Reservation> reservationsForUser(Long userId) {
        return reservationRepository.findByUserIdOrderByReservedAtDesc(userId);
    }

    public Set<Long> reservedBookIdsFor(Long userId) {
        return new HashSet<>(reservationRepository.findBookIdsByUserAndStatus(userId, ReservationStatus.ACTIVE));
    }
}
