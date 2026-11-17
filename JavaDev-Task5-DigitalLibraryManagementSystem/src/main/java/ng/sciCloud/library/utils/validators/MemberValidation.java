package ng.sciCloud.library.utils.validators;

import ng.sciCloud.library.dto.RegisterForm;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Role;
import ng.sciCloud.library.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class MemberValidation {
    private final UserRepository userRepository;

    public MemberValidation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateRegistration(RegisterForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new LibraryException("The two passwords do not match.");
        }
    }

    public void validateEmailAvailable(String email) {
        String cleanEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new LibraryException("An account with this email already exists.");
        }
    }

    public void validateToggleEnabled(AppUser member) {
        if (member.getRole() != Role.USER) {
            throw new LibraryException("Only member accounts can be enabled or disabled here.");
        }
    }

    public AppUser requireMember(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Member not found."));
    }

    public AppUser requireAccountByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new LibraryException("Account not found."));
    }
}
