package ru.javaops.cloudjava.web.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.javaops.cloudjava.model.Role;
import ru.javaops.cloudjava.model.User;
import ru.javaops.cloudjava.repository.UserRepository;
import ru.javaops.cloudjava.web.AbstractControllerTest;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaops.cloudjava.web.user.AdminUserController.REST_URL;
import static ru.javaops.cloudjava.web.user.UniqueMailValidator.EXCEPTION_DUPLICATE_EMAIL;
import static ru.javaops.cloudjava.web.user.UserTestData.*;

class AdminUserControllerTest extends AbstractControllerTest {

    private static final String REST_URL_SLASH = REST_URL + '/';

    @Autowired
    private UserRepository userRepository;

    @Test
    void get() throws Exception {
        performJwt(MockMvcRequestBuilders.get(REST_URL_SLASH + ADMIN_ID), admin)
                .andExpect(status().isOk())
                .andDo(print())
                // https://jira.spring.io/browse/SPR-14472
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHER.contentJson(admin));
    }

    @Test
    void getNotFound() throws Exception {
        performJwt(MockMvcRequestBuilders.get(REST_URL_SLASH + NOT_FOUND), admin)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmail() throws Exception {
        performJwt(MockMvcRequestBuilders.get(REST_URL_SLASH + "by-email?email=" + admin.getEmail()), admin)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHER.contentJson(admin));
    }

    @Test
    void delete() throws Exception {
        performJwt(MockMvcRequestBuilders.delete(REST_URL_SLASH + USER_ID), admin)
                .andDo(print())
                .andExpect(status().isNoContent());
        assertFalse(userRepository.findById(USER_ID).isPresent());
    }

    @Test
    void deleteNotFound() throws Exception {
        performJwt(MockMvcRequestBuilders.delete(REST_URL_SLASH + NOT_FOUND), admin)
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void enableNotFound() throws Exception {
        performJwt(MockMvcRequestBuilders.patch(REST_URL_SLASH + NOT_FOUND)
                .param("enabled", "false")
                .contentType(MediaType.APPLICATION_JSON), admin)
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getForbidden() throws Exception {
        performJwt(MockMvcRequestBuilders.get(REST_URL), user)
                .andExpect(status().isForbidden());
    }

    @Test
    void update() throws Exception {
        User updated = getUpdated();
        updated.setId(null);
        performJwt(MockMvcRequestBuilders.put(REST_URL_SLASH + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(updated, "newPass")), admin)
                .andDo(print())
                .andExpect(status().isNoContent());

        USER_MATCHER.assertMatch(userRepository.getExisted(USER_ID), getUpdated());
    }

    @Test
    void createWithLocation() throws Exception {
        User newUser = getNew();
        ResultActions action = performJwt(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(newUser, "newPass")), admin)
                .andExpect(status().isCreated());

        User created = USER_MATCHER.readFromJson(action);
        int newId = created.id();
        newUser.setId(newId);
        USER_MATCHER.assertMatch(created, newUser);
        USER_MATCHER.assertMatch(userRepository.getExisted(newId), newUser);
    }

    @Test
    void getAll() throws Exception {
        performJwt(MockMvcRequestBuilders.get(REST_URL), admin)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHER.contentJson(admin, user));
    }

    @Test
    void enable() throws Exception {
        performJwt(MockMvcRequestBuilders.patch(REST_URL_SLASH + USER_ID)
                .param("enabled", "false")
                .contentType(MediaType.APPLICATION_JSON), admin)
                .andDo(print())
                .andExpect(status().isNoContent());

        assertFalse(userRepository.getExisted(USER_ID).isEnabled());
    }

    @Test
    void createInvalid() throws Exception {
        User invalid = new User(null, null, "", "newPass", Role.USER, Role.ADMIN);
        performJwt(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(invalid, "newPass")), admin)
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void updateInvalid() throws Exception {
        User invalid = new User(user);
        invalid.setName("");
        performJwt(MockMvcRequestBuilders.put(REST_URL_SLASH + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(invalid, "password")), admin)
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void updateHtmlUnsafe() throws Exception {
        User updated = new User(user);
        updated.setName("<script>alert(123)</script>");
        performJwt(MockMvcRequestBuilders.put(REST_URL_SLASH + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(updated, "password")), admin)
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    void updateDuplicate() throws Exception {
        User updated = new User(user);
        updated.setEmail(ADMIN_MAIL);
        performJwt(MockMvcRequestBuilders.put(REST_URL_SLASH + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(updated, "password")), admin)
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString(EXCEPTION_DUPLICATE_EMAIL)));
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    void createDuplicate() throws Exception {
        User expected = new User(null, "New", USER_MAIL, "newPass", Role.USER, Role.ADMIN);
        performJwt(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(expected, "newPass")), admin)
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString(EXCEPTION_DUPLICATE_EMAIL)));
    }
}