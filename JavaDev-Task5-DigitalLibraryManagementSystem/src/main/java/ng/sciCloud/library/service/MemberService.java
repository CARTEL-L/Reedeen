package ng.sciCloud.library.service;

import ng.sciCloud.library.dto.RegisterForm;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Role;
import ng.sciCloud.library.repository.UserRepository;
import ng.sciCloud.library.utils.validators.MemberValidation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidation validation;

    public MemberService(UserRepository userRepository, PasswordEncoder passwordEncoder, MemberValidation validation) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validation = validation;
    }

    @Transactional
    public AppUser register(RegisterForm form) {
        validation.validateRegistration(form);
        return create(form.getFullName(), form.getEmail(), form.getPassword(), Role.USER, false);
    }

    @Transactional
    public AppUser createUser(String fullName, String email, String rawPassword, Role role) {
        return create(fullName, email, rawPassword, role, true);
    }

    private AppUser create(String fullName, String email, String rawPassword, Role role, boolean emailVerified) {
        String cleanEmail = email.trim().toLowerCase();
        validation.validateEmailAvailable(cleanEmail);
        AppUser user = new AppUser();
        user.setFullName(fullName.trim());
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEmailVerified(emailVerified);
        return userRepository.save(user);
    }

    public AppUser requireByEmail(String email) {
        return validation.requireAccountByEmail(email);
    }

    public AppUser get(Long id) {
        return validation.requireMember(id);
    }

    public List<AppUser> listMembers() {
        return userRepository.findByRoleOrderByFullNameAsc(Role.USER);
    }

    @Transactional
    public AppUser toggleEnabled(Long id) {
        AppUser member = get(id);
        validation.validateToggleEnabled(member);
        member.setEnabled(!member.isEnabled());
        return member;
    }
}
