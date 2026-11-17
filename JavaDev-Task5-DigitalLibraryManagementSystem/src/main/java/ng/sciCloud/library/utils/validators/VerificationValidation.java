package ng.sciCloud.library.utils.validators;

import ng.sciCloud.library.config.VerificationProperties;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.VerificationCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class VerificationValidation {

    private static final String BAD_CODE = "That code is incorrect or has expired. Request a new one if you need to.";

    private final PasswordEncoder passwordEncoder;
    private final VerificationProperties properties;

    public VerificationValidation(PasswordEncoder passwordEncoder, VerificationProperties properties) {
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    public void validateCodeFormat(String code) {
        if (code == null || !code.trim().matches("\\d{6}")) {
            throw new LibraryException("Enter the 6-digit code from your email.");
        }
    }

    public void validateResendAllowed(Optional<VerificationCode> latest, LocalDateTime now) {
        latest.ifPresent(previous -> {
            long waited = Duration.between(previous.getCreatedAt(), now).getSeconds();
            long remaining = properties.getResendSeconds() - waited;
            if (remaining > 0) {
                throw new LibraryException("Please wait " + remaining + " seconds before asking for another code.");
            }
        });
    }

    public void validateCode(VerificationCode code, String submitted, LocalDateTime now) {
        if (code == null || code.isUsed() || now.isAfter(code.getExpiresAt())
                || code.getAttempts() >= properties.getMaxAttempts()) {
            throw new LibraryException(BAD_CODE);
        }
        if (!passwordEncoder.matches(submitted.trim(), code.getCodeHash())) {
            code.setAttempts(code.getAttempts() + 1);
            if (code.getAttempts() >= properties.getMaxAttempts()) {
                code.setUsed(true);
            }
            throw new LibraryException(BAD_CODE);
        }
    }

    public void validateNewPassword(String password, String confirmation) {
        if (!password.equals(confirmation)) {
            throw new LibraryException("The two passwords do not match.");
        }
    }
}
