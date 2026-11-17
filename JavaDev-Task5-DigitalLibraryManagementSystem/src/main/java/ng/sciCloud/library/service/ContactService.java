package ng.sciCloud.library.service;

import ng.sciCloud.library.dto.ContactForm;
import ng.sciCloud.library.utils.validators.ContactValidation;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.ContactMessage;
import ng.sciCloud.library.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContactService {

    private final ContactMessageRepository repository;
    private final ContactValidation validation;

    public ContactService(ContactMessageRepository repository, ContactValidation validation) {
        this.repository = repository;
        this.validation = validation;
    }

    @Transactional
    public ContactMessage submit(AppUser user, ContactForm form) {
        ContactMessage message = new ContactMessage();
        message.setUser(user);
        message.setName(user.getFullName());
        message.setEmail(user.getEmail());
        message.setSubject(form.getSubject().trim());
        message.setMessage(form.getMessage().trim());
        return repository.save(message);
    }

    public List<ContactMessage> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void markResolved(Long id) {
        ContactMessage message = validation.requireMessage(id);
        message.setResolved(true);
    }
}
