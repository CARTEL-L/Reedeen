package ng.sciCloud.library.service;

import ng.sciCloud.library.dto.BookForm;
import ng.sciCloud.library.model.Book;
import ng.sciCloud.library.repository.BookRepository;
import ng.sciCloud.library.repository.ReservationRepository;
import ng.sciCloud.library.utils.validators.BookValidation;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final ReservationRepository reservationRepository;
    private final BookValidation validation;

    public BookService(BookRepository bookRepository,
                       ReservationRepository reservationRepository,
                       BookValidation validation) {
        this.bookRepository = bookRepository;
        this.reservationRepository = reservationRepository;
        this.validation = validation;
    }

    public List<Book> search(String query, String category) {
        String q = query == null ? "" : query.trim();
        String c = category == null ? "" : category.trim();
        return bookRepository.search(q, c);
    }

    public List<String> categories() {
        return bookRepository.findAllCategories();
    }

    public List<Book> findAll() {
        return bookRepository.findAll(Sort.by("title"));
    }

    public Book get(Long id) {
        return validation.requireBook(id);
    }

    @Transactional
    public Book create(BookForm form) {
        String isbn = validation.normalizeIsbn(form.getIsbn());
        validation.validateCreate(form, isbn);
        Book book = new Book();
        copyFields(book, form, isbn);
        book.setTotalQuantity(form.getQuantity());
        book.setAvailableQuantity(form.getQuantity());
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, BookForm form) {
        Book book = validation.requireBookForUpdate(id);
        String isbn = validation.normalizeIsbn(form.getIsbn());
        long issued = validation.validateUpdate(id, form, isbn);
        copyFields(book, form, isbn);
        book.setTotalQuantity(form.getQuantity());
        book.setAvailableQuantity((int) (form.getQuantity() - issued));
        return book;
    }

    @Transactional
    public void delete(Long id) {
        Book book = get(id);
        validation.validateDelete(id);
        reservationRepository.deleteByBookId(id);
        bookRepository.delete(book);
    }

    private void copyFields(Book book, BookForm form, String isbn) {
        book.setTitle(form.getTitle().trim());
        book.setAuthor(form.getAuthor().trim());
        book.setIsbn(isbn);
        book.setCategory(form.getCategory().trim());
    }
}
