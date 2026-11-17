package ng.sciCloud.library.mail;

public interface EmailSender {

    void send(String toEmail, String subject, String body);
}
