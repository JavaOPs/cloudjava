package ru.javaops.cloudjava.web.user;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import ru.javaops.cloudjava.error.IllegalRequestDataException;
import ru.javaops.cloudjava.model.User;
import ru.javaops.cloudjava.repository.UserRepository;
import ru.javaops.cloudjava.util.UserUtil;
import ru.javaops.cloudjava.web.JwtUser;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractUserController {

    protected final Logger log = getLogger(getClass());

    @Autowired
    protected UserRepository repository;

    @Autowired
    private UniqueMailValidator emailValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
    }

    public ResponseEntity<User> get(int id) {
        log.info("get {}", id);
        return ResponseEntity.of(repository.findById(id));
    }

    public void delete(int id) {
        log.info("delete {}", id);
        repository.deleteExisted(id);
    }

    protected User prepareAndSave(User user) {
        return repository.save(UserUtil.prepareToSave(user));
    }

    protected User findByJwtUser(JwtUser jwtUser) {
        return repository.findById(jwtUser.id()).orElseThrow(
                () -> new IllegalRequestDataException("User id='" + jwtUser.getId() + "' was not found"));
    }
}