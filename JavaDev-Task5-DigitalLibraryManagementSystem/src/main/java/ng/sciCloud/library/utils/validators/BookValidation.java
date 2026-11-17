package ng.sciCloud.library.utils.validators;

import ng.sciCloud.library.dto.BookForm;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.Book;
import ng.sciCloud.library.model.LoanStatus;
import ng.sciCloud.library.repository.BookRepository;
import ng.sciCloud.library.repository.LoanRepository;
import org.springframework.stereotype.Component;

@Component
public class BookValidation {
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public BookValidation(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    public String normalizeIsbn(String raw) {
        String isbn = raw.replace("-", "").replace(" ", "").toUpperCase();
        if (isbn.length() != 10 && isbn.length() != 13) {
            throw new LibraryException("ISBN must have 10 or 13 characters (hyphens are ignored).");
        }
        return isbn;
    }

    public void validateCreate(BookForm form, String isbn) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new LibraryException("A book with ISBN " + isbn + " already exists.");
        }
    }

    public long validateUpdate(Long id, BookForm form, String isbn) {
        bookRepository.findByIsbn(isbn).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new LibraryException("Another book already uses ISBN " + isbn + ".");
            }
        });
        long issued = loanRepository.countByBookIdAndStatus(id, LoanStatus.ISSUED);
        if (form.getQuantity() < issued) {
            throw new LibraryException("Quantity cannot be lower than the " + issued
                    + " copies that are currently issued.");
        }
        return issued;
    }

    public void validateDelete(Long id) {
        if (loanRepository.existsByBookId(id)) {
            throw new LibraryException("This book has loan history and cannot be deleted. "
                    + "Set its quantity to 0 to take it off the shelf instead.");
        }
    }

    public Book requireBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Book not found."));
    }

    public Book requireBookForUpdate(Long id) {
        return bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new LibraryException("Book not found."));
    }
}
