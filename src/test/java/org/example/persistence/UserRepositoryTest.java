package org.example.persistence;

import org.example.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ContextConfiguration(classes = {UserRepository.class})
@Sql(scripts = {"/schema.sql"})
public class UserRepositoryTest {

    @Autowired
    private DataSource dataSource;

    private UserRepository userRepository;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        userRepository = new UserRepository(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DROP ALL OBJECTS");
    }

    @Test
    public void testAddUser() {
        User newUser = new User("testuser", "password123", "Test User");
        Optional<User> addedUserOpt = userRepository.add(newUser);

        assertTrue(addedUserOpt.isPresent(), "User should be added successfully");
        assertNotNull(addedUserOpt.get().getId(), "Added user should have a non-null ID");
        assertEquals("testuser", addedUserOpt.get().getUsername());
    }

    @Test
    public void testAddUserFailsOnDuplicateUsername() {
        User user1 = new User("duplicateuser", "pass1", "User One");
        userRepository.add(user1);

        User user2 = new User("duplicateuser", "pass2", "User Two");
        assertThrows(RepositoryException.class, () -> {
            userRepository.add(user2);
        }, "Adding a user with a duplicate username should throw an exception");
    }

    @Test
    public void testFindOneById() {
        User newUser = new User("findme", "pass", "Find Me");
        User addedUser = userRepository.add(newUser).orElseThrow();

        Optional<User> foundUserOpt = userRepository.findOne(addedUser.getId());

        assertTrue(foundUserOpt.isPresent(), "User should be found by ID");
        assertEquals(addedUser.getId(), foundUserOpt.get().getId());
        assertEquals("findme", foundUserOpt.get().getUsername());
    }

    @Test
    public void testFindOneByIdNotFound() {
        Optional<User> foundUserOpt = userRepository.findOne(999L);
        assertTrue(foundUserOpt.isEmpty(), "Should not find a user with a non-existent ID");
    }

    @Test
    public void testFindOneByUsername() {
        User newUser = new User("findbyusername", "pass", "Find By Username");
        userRepository.add(newUser);

        Optional<User> foundUserOpt = userRepository.findOneByUsername("findbyusername");

        assertTrue(foundUserOpt.isPresent(), "User should be found by username");
        assertEquals("findbyusername", foundUserOpt.get().getUsername());
    }

    @Test
    public void testFindOneByUsernameNotFound() {
        Optional<User> foundUserOpt = userRepository.findOneByUsername("nonexistentuser");
        assertTrue(foundUserOpt.isEmpty(), "Should not find a user with a non-existent username");
    }

    @Test
    public void testGetAll() {
        userRepository.add(new User("user1", "pass1", "User One"));
        userRepository.add(new User("user2", "pass2", "User Two"));

        Iterable<User> usersIterable = userRepository.getAll();
        List<User> users = StreamSupport.stream(usersIterable.spliterator(), false).toList();

        assertNotNull(users);
        assertEquals(2, users.size(), "Should retrieve all two users");
    }

    @Test
    public void testGetAllWhenEmpty() {
        Iterable<User> usersIterable = userRepository.getAll();
        List<User> users = StreamSupport.stream(usersIterable.spliterator(), false).toList();

        assertNotNull(users);
        assertTrue(users.isEmpty(), "Should return an empty list when no users exist");
    }

    @Test
    public void testDeleteUser() {
        User newUser = new User("deleteme", "pass", "Delete Me");
        User addedUser = userRepository.add(newUser).orElseThrow();
        Long userId = addedUser.getId();

        Optional<User> deletedUserOpt = userRepository.delete(userId);

        assertTrue(deletedUserOpt.isPresent(), "Delete should return the deleted user");
        assertEquals(userId, deletedUserOpt.get().getId());

        Optional<User> findAfterDeleteOpt = userRepository.findOne(userId);
        assertTrue(findAfterDeleteOpt.isEmpty(), "User should not be found after deletion");
    }

    @Test
    public void testDeleteNonExistentUser() {
        Optional<User> deletedUserOpt = userRepository.delete(999L);
        assertTrue(deletedUserOpt.isEmpty(), "Deleting a non-existent user should return an empty optional");
    }
}