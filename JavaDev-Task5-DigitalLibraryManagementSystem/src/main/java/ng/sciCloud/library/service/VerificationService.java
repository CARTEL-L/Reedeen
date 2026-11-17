package ng.sciCloud.library.service;

import ng.sciCloud.library.config.VerificationProperties;
import ng.sciCloud.library.dto.ResetPasswordForm;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.mail.EmailSender;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.CodePurpose;
import ng.sciCloud.library.model.VerificationCode;
import ng.sciCloud.library.repository.UserRepository;
import ng.sciCloud.library.repository.VerificationCodeRepository;
import ng.sciCloud.library.utils.validators.MemberValidation;
import ng.sciCloud.library.utils.validators.VerificationValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final VerificationCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final VerificationProperties properties;
    private final VerificationValidation validation;
    private final MemberValidation memberValidation;

    public VerificationService(VerificationCodeRepository codeRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               EmailSender emailSender,
                               VerificationProperties properties,
                               VerificationValidation validation,
                               MemberValidation memberValidation) {
        this.codeRepository = codeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.properties = properties;
        this.validation = validation;
        this.memberValidation = memberValidation;
    }

    @Transactional
    public void sendVerificationCode(String email) {
        AppUser user = memberValidation.requireAccountByEmail(email.trim());
        if (user.isEmailVerified()) {
            throw new LibraryException("This email is already verified. You can sign in.");
        }
        issueCode(user, CodePurpose.EMAIL_VERIFICATION);
    }

    @Transactional(noRollbackFor = LibraryException.class)
    public void confirmEmail(String email, String code) {
        validation.validateCodeFormat(code);
        AppUser user = memberValidation.requireAccountByEmail(email.trim());
        if (user.isEmailVerified()) {
            return;
        }
        VerificationCode latest = codeRepository
                .findFirstByUserIdAndPurposeAndUsedFalseOrderByIdDesc(user.getId(), CodePurpose.EMAIL_VERIFICATION)
                .orElse(null);
        validation.validateCode(latest, code, LocalDateTime.now());
        latest.setUsed(true);
        user.setEmailVerified(true);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<AppUser> found = userRepository.findByEmailIgnoreCase(email.trim());
        if (found.isEmpty() || !found.get().isEnabled()) {
            return;
        }
        try {
            issueCode(found.get(), CodePurpose.PASSWORD_RESET);
        } catch (LibraryException e) {
            log.info("Password reset code not sent to {}: {}", found.get().getEmail(), e.getMessage());
        }
    }

    @Transactional(noRollbackFor = LibraryException.class)
    public void resetPassword(ResetPasswordForm form) {
        validation.validateNewPassword(form.getPassword(), form.getConfirmPassword());
        AppUser user = userRepository.findByEmailIgnoreCase(form.getEmail().trim()).orElse(null);
        VerificationCode latest = null;
        if (user != null && user.isEnabled()) {
            latest = codeRepository
                    .findFirstByUserIdAndPurposeAndUsedFalseOrderByIdDesc(user.getId(), CodePurpose.PASSWORD_RESET)
                    .orElse(null);
        }
        validation.validateCode(latest, form.getCode(), LocalDateTime.now());
        latest.setUsed(true);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEmailVerified(true);
    }

    private void issueCode(AppUser user, CodePurpose purpose) {
        LocalDateTime now = LocalDateTime.now();
        validation.validateResendAllowed(
                codeRepository.findFirstByUserIdAndPurposeOrderByIdDesc(user.getId(), purpose), now);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        emailSender.send(user.getEmail(), subjectFor(purpose), bodyFor(user, purpose, code));

        codeRepository.findByUserIdAndPurposeAndUsedFalse(user.getId(), purpose)
                .forEach(previous -> previous.setUsed(true));
        VerificationCode entity = new VerificationCode();
        entity.setUser(user);
        entity.setPurpose(purpose);
        entity.setCodeHash(passwordEncoder.encode(code));
        entity.setCreatedAt(now);
        entity.setExpiresAt(now.plusMinutes(properties.getCodeMinutes()));
        codeRepository.save(entity);
    }

    private String subjectFor(CodePurpose purpose) {
        return purpose == CodePurpose.EMAIL_VERIFICATION
                ? "Your email verification code"
                : "Your password reset code";
    }

    private String bodyFor(AppUser user, CodePurpose purpose, String code) {
        String action = purpose == CodePurpose.EMAIL_VERIFICATION
                ? "verify your email address"
                : "reset your password";
        return "Hello " + user.getFullName() + ",\n\n"
                + "Use this code to " + action + ":\n\n"
                + "    " + code + "\n\n"
                + "It expires in " + properties.getCodeMinutes() + " minutes and works once.\n"
                + "If you did not ask for this, you can ignore this email.\n\n"
                + "The Reading Room";
    }
}
