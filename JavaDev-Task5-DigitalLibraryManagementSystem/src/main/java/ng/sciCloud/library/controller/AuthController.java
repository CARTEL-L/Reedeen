package ng.sciCloud.library.controller;

import jakarta.validation.Valid;
import ng.sciCloud.library.dto.ForgotPasswordForm;
import ng.sciCloud.library.dto.RegisterForm;
import ng.sciCloud.library.dto.ResetPasswordForm;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.service.MemberService;
import ng.sciCloud.library.service.VerificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final MemberService memberService;
    private final VerificationService verificationService;

    public AuthController(MemberService memberService, VerificationService verificationService) {
        this.memberService = memberService;
        this.verificationService = verificationService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            memberService.register(form);
        } catch (LibraryException e) {
            result.reject("register.failed", e.getMessage());
            return "register";
        }
        String email = form.getEmail().trim().toLowerCase();
        try {
            verificationService.sendVerificationCode(email);
            redirectAttributes.addFlashAttribute("success",
                    "Account created. We emailed a 6-digit code to " + email + ".");
        } catch (LibraryException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Account created, but the code could not be sent. " + e.getMessage()
                            + " Use \"Send a new code\" below.");
        }
        redirectAttributes.addAttribute("email", email);
        return "redirect:/verify";
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam(defaultValue = "") String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String email,
                         @RequestParam String code,
                         RedirectAttributes redirectAttributes) {
        verificationService.confirmEmail(email, code);
        redirectAttributes.addFlashAttribute("success", "Email verified. You can sign in now.");
        return "redirect:/login";
    }

    @PostMapping("/verify/resend")
    public String resend(@RequestParam String email, RedirectAttributes redirectAttributes) {
        verificationService.sendVerificationCode(email);
        redirectAttributes.addFlashAttribute("success", "A new code is on its way to " + email.trim() + ".");
        redirectAttributes.addAttribute("email", email.trim());
        return "redirect:/verify";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        model.addAttribute("form", new ForgotPasswordForm());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("form") ForgotPasswordForm form,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "forgot-password";
        }
        verificationService.requestPasswordReset(form.getEmail());
        redirectAttributes.addFlashAttribute("success",
                "If an account exists for that email, we have sent it a 6-digit code.");
        redirectAttributes.addAttribute("email", form.getEmail().trim().toLowerCase());
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(defaultValue = "") String email, Model model) {
        ResetPasswordForm form = new ResetPasswordForm();
        form.setEmail(email);
        model.addAttribute("form", form);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("form") ResetPasswordForm form,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "reset-password";
        }
        try {
            verificationService.resetPassword(form);
        } catch (LibraryException e) {
            result.reject("reset.failed", e.getMessage());
            return "reset-password";
        }
        redirectAttributes.addFlashAttribute("success", "Password changed. You can sign in with the new password.");
        return "redirect:/login";
    }
}
