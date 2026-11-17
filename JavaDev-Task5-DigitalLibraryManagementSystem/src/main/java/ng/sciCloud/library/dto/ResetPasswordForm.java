package ng.sciCloud.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordForm {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Enter the code from your email")
    @Pattern(regexp = "\\d{6}", message = "The code has 6 digits")
    private String code;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

}
