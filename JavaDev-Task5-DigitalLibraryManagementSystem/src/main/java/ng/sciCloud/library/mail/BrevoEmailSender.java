package ng.sciCloud.library.mail;

import ng.sciCloud.library.config.MailProperties;
import ng.sciCloud.library.exception.LibraryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "library.mail.provider", havingValue = "brevo")
public class BrevoEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailSender.class);

    private final RestClient client;
    private final MailProperties properties;

    public BrevoEmailSender(RestClient.Builder builder, MailProperties properties) {
        if (properties.getBrevoApiKey() == null || properties.getBrevoApiKey().isBlank()) {
            throw new IllegalStateException("library.mail.provider=brevo needs BREVO_API_KEY to be set.");
        }
        this.properties = properties;
        this.client = builder.baseUrl(properties.getBrevoUrl()).build();
    }

    @Override
    public void send(String toEmail, String subject, String body) {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", properties.getFromName(), "email", properties.getFromEmail()),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "textContent", body);
        try {
            client.post()
                    .uri("/v3/smtp/email")
                    .header("api-key", properties.getBrevoApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn("Brevo rejected the email: HTTP {} {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new LibraryException("We could not send the email right now. Please try again in a moment.");
        } catch (RestClientException e) {
            log.warn("Brevo could not be reached: {}", e.getMessage());
            throw new LibraryException("We could not send the email right now. Please try again in a moment.");
        }
    }
}
