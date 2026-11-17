package ng.sciCloud.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactForm {

    @NotBlank(message = "Subject is required")
    @Size(max = 150, message = "Subject is too long")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must be 2000 characters or fewer")
    private String message;

}
