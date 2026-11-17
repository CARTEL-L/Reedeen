package ng.sciCloud.library.service;

import ng.sciCloud.library.config.LibraryProperties;
import ng.sciCloud.library.model.*;
import ng.sciCloud.library.repository.LoanRepository;
import ng.sciCloud.library.utils.validators.LoanValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class LoanService {

    private final LoanRepository loanRepository;
    private final FineCalculator fineCalculator;
    private final LibraryProperties properties;
    private final LoanValidation validation;

    public LoanService(LoanRepository loanRepository,
                       FineCalculator fineCalculator,
                       LibraryProperties properties,
                       LoanValidation validation) {
        this.loanRepository = loanRepository;
        this.fineCalculator = fineCalculator;
        this.properties = properties;
        this.validation = validation;
    }

    @Transactional
    public Loan issueBook(Long userId, Long bookId) {
        AppUser user = validation.requireUser(userId);
        Book book = validation.requireBookForUpdate(bookId);
        LocalDate today = LocalDate.now();
        validation.validateIssue(userId, bookId, book);

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setIssueDate(today);
        loan.setDueDate(today.plusDays(properties.getLoanDays()));
        loan.setStatus(LoanStatus.ISSUED);
        return loanRepository.save(loan);
    }


    @Transactional
    public Loan returnBook(Long loanId, Long userId) {
        Loan loan = validation.requireLoan(loanId);
        validation.validateReturn(loan, userId);

        Book book = validation.requireBookForUpdate(loan.getBook().getId());

        LocalDate today = LocalDate.now();
        BigDecimal fine = fineCalculator.calculate(loan.getDueDate(), today);

        loan.setReturnDate(today);
        loan.setStatus(LoanStatus.RETURNED);
        if (fine.signum() > 0) {
            loan.setFineAmount(fine);
            loan.setFineStatus(FineStatus.UNPAID);
        }
        book.setAvailableQuantity(Math.min(book.getTotalQuantity(), book.getAvailableQuantity() + 1));
        return loan;
    }


    @Transactional
    public Loan markFinePaid(Long loanId) {
        Loan loan = validation.requireLoan(loanId);
        validation.validateFinePayment(loan);
        loan.setFineStatus(FineStatus.PAID);
        loan.setFinePaidAt(LocalDateTime.now());
        return loan;
    }

    public BigDecimal totalFines(FineStatus status) {
        BigDecimal total = loanRepository.sumFineAmountByStatus(status);
        return total == null ? BigDecimal.ZERO.setScale(2) : total;
    }


    public List<Loan> loansForUser(Long userId) {
        return loanRepository.findByUserIdOrderByIssueDateDescIdDesc(userId);
    }

    public List<Loan> issuedLoans() {
        return loanRepository.findByStatusOrderByDueDateAsc(LoanStatus.ISSUED);
    }

    public List<Loan> loansWithFines() {
        return loanRepository.findByFineStatusNotOrderByIdDesc(FineStatus.NONE);
    }

    public Set<Long> issuedBookIdsFor(Long userId) {
        return new HashSet<>(loanRepository.findBookIdsByUserAndStatus(userId, LoanStatus.ISSUED));
    }
}
