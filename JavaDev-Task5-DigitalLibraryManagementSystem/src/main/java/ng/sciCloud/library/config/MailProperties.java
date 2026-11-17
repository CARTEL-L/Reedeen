package ng.sciCloud.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library.mail")
public class MailProperties {

    private String provider = "log";
    private String brevoApiKey = "";
    private String brevoUrl = "https://api.brevo.com";
    private String fromEmail = "no-reply@example.com";
    private String fromName = "The Reading Room";

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getBrevoApiKey() { return brevoApiKey; }
    public void setBrevoApiKey(String brevoApiKey) { this.brevoApiKey = brevoApiKey; }

    public String getBrevoUrl() { return brevoUrl; }
    public void setBrevoUrl(String brevoUrl) { this.brevoUrl = brevoUrl; }

    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }
}
