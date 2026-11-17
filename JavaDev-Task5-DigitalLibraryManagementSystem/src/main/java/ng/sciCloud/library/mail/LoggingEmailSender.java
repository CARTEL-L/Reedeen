package ng.sciCloud.library.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "library.mail.provider", havingValue = "log", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String toEmail, String subject, String body) {
        log.warn("EMAIL NOT SENT: library.mail.provider is 'log'. Set MAIL_PROVIDER=brevo, BREVO_API_KEY and MAIL_FROM to send real email.");
        log.info("EMAIL (not sent, library.mail.provider=log) to={} subject={}\n{}", toEmail, subject, body);
    }
}
