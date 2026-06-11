package worldcupbraket.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import worldcupbraket.model.User;
import worldcupbraket.model.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String name, String password) {
        User user = new User(
                name,
                passwordEncoder.encode(password)
        );

        return userRepository.save(user);
    }

    public boolean authenticate(String name, String password) {
        User user = userRepository.findFirstByName(name);
        if (user == null) {
            return false;
        }

        return passwordEncoder.matches(password, user.getPassword());
    }

    public User getUserByUsername(String username) {
        return userRepository.findFirstByName(username);
    }

    public boolean isAdmin(String name) {
        return "marcos".equals(name);
    }
}