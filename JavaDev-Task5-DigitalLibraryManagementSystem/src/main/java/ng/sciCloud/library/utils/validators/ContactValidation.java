package ng.sciCloud.library.utils.validators;

import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.ContactMessage;
import ng.sciCloud.library.repository.ContactMessageRepository;
import org.springframework.stereotype.Component;

@Component
public class ContactValidation {
    private final ContactMessageRepository repository;

    public ContactValidation(ContactMessageRepository repository) {
        this.repository = repository;
    }

    public ContactMessage requireMessage(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new LibraryException("Message not found."));
    }
}
