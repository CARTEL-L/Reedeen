package ng.sciCloud.library.controller;

import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Book;
import ng.sciCloud.library.model.Loan;
import ng.sciCloud.library.service.BookService;
import ng.sciCloud.library.service.LoanService;
import ng.sciCloud.library.service.MemberService;
import ng.sciCloud.library.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class CatalogController {

    private final BookService bookService;
    private final LoanService loanService;
    private final ReservationService reservationService;
    private final MemberService memberService;

    public CatalogController(BookService bookService,
                             LoanService loanService,
                             ReservationService reservationService,
                             MemberService memberService) {
        this.bookService = bookService;
        this.loanService = loanService;
        this.reservationService = reservationService;
        this.memberService = memberService;
    }

    @GetMapping("/books")
    public String catalogue(@RequestParam(required = false) String q,
                            @RequestParam(required = false) String category,
                            Model model,
                            Principal principal) {
        AppUser user = memberService.requireByEmail(principal.getName());
        List<Book> books = bookService.search(q, category);

        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.categories());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("selectedCategory", category == null ? "" : category);
        model.addAttribute("issuedBookIds", loanService.issuedBookIdsFor(user.getId()));
        model.addAttribute("reservedBookIds", reservationService.reservedBookIdsFor(user.getId()));
        return "books/catalog";
    }

    @PostMapping("/books/{id}/issue")
    public String issue(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        AppUser user = memberService.requireByEmail(principal.getName());
        Loan loan = loanService.issueBook(user.getId(), id);
        redirectAttributes.addFlashAttribute("success", "\"" + loan.getBook().getTitle()
                + "\" issued. Please return it by "
                + loan.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + ".");
        return "redirect:/my/loans";
    }

    @PostMapping("/books/{id}/reserve")
    public String reserve(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        AppUser user = memberService.requireByEmail(principal.getName());
        reservationService.reserve(user.getId(), id);
        redirectAttributes.addFlashAttribute("success",
                "Book reserved. Issue it from the catalogue when a copy becomes free.");
        return "redirect:/my/reservations";
    }
}
