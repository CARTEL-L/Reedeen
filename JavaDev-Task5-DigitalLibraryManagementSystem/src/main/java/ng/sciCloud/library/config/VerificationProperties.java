package ng.sciCloud.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library.verification")
public class VerificationProperties {

    private int codeMinutes = 10;
    private int resendSeconds = 60;
    private int maxAttempts = 5;

    public int getCodeMinutes() { return codeMinutes; }
    public void setCodeMinutes(int codeMinutes) { this.codeMinutes = codeMinutes; }

    public int getResendSeconds() { return resendSeconds; }
    public void setResendSeconds(int resendSeconds) { this.resendSeconds = resendSeconds; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
}
