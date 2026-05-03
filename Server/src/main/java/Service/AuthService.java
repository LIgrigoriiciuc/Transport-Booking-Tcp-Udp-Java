package Service;


import Domain.Office;
import Domain.User;
import Repository.Filter;
import Repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Optional;

public class AuthService extends GenericService<Long, User> {

    private static final Logger logger = LogManager.getLogger(AuthService.class);

    private static final int BCRYPT_ROUNDS = 12;

    public AuthService(UserRepository repository) {
        super(repository);
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    public static boolean checkPassword(String plainPassword, String hashed) {
        return BCrypt.checkpw(plainPassword, hashed);
    }
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            logger.warn("Username and password are required");
            throw new IllegalArgumentException("Username and password are required.");
        }
        Filter f = new Filter();
        f.addFilter("username", username);
        List<User> matches = repository.filter(f);
        if (matches.isEmpty()) {
            logger.warn("Incorrect credentials for username {}", username);
            throw new RuntimeException("Incorrect credentials.");
        }
        User user = matches.get(0);
        String stored = user.getPassword();
        boolean valid;
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            valid = checkPassword(password, stored);
        } else {
            valid = stored.equals(password);
            if (valid) {
                user.setPassword(hashPassword(password));
                repository.update(user);
            }
        }
        if (!valid) {
            logger.warn("Incorrect credentials for username {}", username);
            throw new RuntimeException("Incorrect credentials.");
        }
        logger.info("User {} logged in", username);
        return user;
    }
}
