package ng.sciCloud.library;

import ng.sciCloud.library.config.LibraryProperties;
import ng.sciCloud.library.config.MailProperties;
import ng.sciCloud.library.config.VerificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({LibraryProperties.class, MailProperties.class, VerificationProperties.class})
public class DigitalLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalLibraryApplication.class, args);
    }
}
