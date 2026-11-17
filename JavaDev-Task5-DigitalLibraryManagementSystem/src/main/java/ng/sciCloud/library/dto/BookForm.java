package ng.sciCloud.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ng.sciCloud.library.model.Book;

@Data
public class BookForm {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title is too long")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 150, message = "Author is too long")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^[0-9Xx\\- ]{10,17}$", message = "ISBN must be 10 or 13 digits")
    private String isbn;

    @NotBlank(message = "Category is required")
    @Size(max = 60, message = "Category is too long")
    private String category;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 10000, message = "Quantity is too large")
    private Integer quantity;

    public static BookForm from(Book book) {
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());
        form.setCategory(book.getCategory());
        form.setQuantity(book.getTotalQuantity());
        return form;
    }

}
