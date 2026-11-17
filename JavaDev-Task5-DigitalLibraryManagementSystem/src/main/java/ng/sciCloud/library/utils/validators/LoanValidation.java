package ng.sciCloud.library.utils.validators;

import ng.sciCloud.library.config.LibraryProperties;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.*;
import ng.sciCloud.library.repository.BookRepository;
import ng.sciCloud.library.repository.LoanRepository;
import ng.sciCloud.library.repository.ReservationRepository;
import ng.sciCloud.library.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LoanValidation {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final LibraryProperties properties;

    public LoanValidation(LoanRepository loanRepository, BookRepository bookRepository,
                          UserRepository userRepository, ReservationRepository reservationRepository,
                          LibraryProperties properties) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.properties = properties;
    }

    public AppUser requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Account not found."));
    }

    public Loan requireLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Loan not found."));
    }

    public Book requireBookForUpdate(Long id) {
        return bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new LibraryException("Book not found."));
    }

    public void validateIssue(Long userId, Long bookId, Book book) {
        LocalDate today = LocalDate.now();
        if (loanRepository.existsByUserIdAndFineStatus(userId, FineStatus.UNPAID)) {
            throw new LibraryException("You have an unpaid fine. Please pay it at the library desk before borrowing again.");
        }
        if (loanRepository.existsByUserIdAndStatusAndDueDateBefore(userId, LoanStatus.ISSUED, today)) {
            throw new LibraryException("You have an overdue book. Please return it before borrowing another.");
        }
        if (loanRepository.countByUserIdAndStatus(userId, LoanStatus.ISSUED) >= properties.getMaxActiveLoans()) {
            throw new LibraryException("You have reached the limit of " + properties.getMaxActiveLoans()
                    + " books at a time.");
        }
        if (loanRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.ISSUED)) {
            throw new LibraryException("You already have a copy of this book.");
        }
        if (book.getAvailableQuantity() <= 0) {
            throw new LibraryException("No copies are available right now. You can reserve this book instead.");
        }
        var queue = reservationRepository.findByBookIdAndStatusOrderByReservedAtAscIdAsc(
                bookId, ReservationStatus.ACTIVE);
        var mine = queue.stream().filter(r -> r.getUser().getId().equals(userId)).findFirst().orElse(null);
        int placeInLine = mine == null ? queue.size() : queue.indexOf(mine);
        if (placeInLine >= book.getAvailableQuantity()) {
            throw new LibraryException("The free copies are being held for members who reserved this book earlier.");
        }
        if (mine != null) {
            mine.setStatus(ReservationStatus.FULFILLED);
        }
    }

    public void validateReturn(Loan loan, Long userId) {
        if (!loan.getUser().getId().equals(userId)) {
            throw new LibraryException("That loan does not belong to you.");
        }
        if (loan.getStatus() != LoanStatus.ISSUED) {
            throw new LibraryException("This book has already been returned.");
        }
    }

    public void validateFinePayment(Loan loan) {
        if (loan.getFineStatus() != FineStatus.UNPAID) {
            throw new LibraryException("This loan has no unpaid fine.");
        }
    }
}
