package ng.sciCloud.library.utils.validators;

import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.*;
import ng.sciCloud.library.repository.BookRepository;
import ng.sciCloud.library.repository.LoanRepository;
import ng.sciCloud.library.repository.ReservationRepository;
import ng.sciCloud.library.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class ReservationValidation {
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public ReservationValidation(ReservationRepository reservationRepository, BookRepository bookRepository,
                                 UserRepository userRepository, LoanRepository loanRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    public AppUser requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Account not found."));
    }

    public Book requireBookForUpdate(Long id) {
        return bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new LibraryException("Book not found."));
    }

    public Reservation requireReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Reservation not found."));
    }

    public void validateReserve(Long userId, Long bookId, Book book) {
        if (book.getAvailableQuantity() > 0) {
            throw new LibraryException("Copies are available right now, so you can issue this book directly.");
        }
        if (loanRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.ISSUED)) {
            throw new LibraryException("You already have this book issued.");
        }
        if (reservationRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, ReservationStatus.ACTIVE)) {
            throw new LibraryException("You have already reserved this book.");
        }
    }

    public void validateCancel(Reservation reservation, Long userId) {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new LibraryException("That reservation does not belong to you.");
        }
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new LibraryException("Only active reservations can be cancelled.");
        }
    }
}
