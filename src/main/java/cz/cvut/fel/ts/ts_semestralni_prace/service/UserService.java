package cz.cvut.fel.ts.ts_semestralni_prace.service;

import cz.cvut.fel.ts.ts_semestralni_prace.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private static final String FILENAME = "users.json";
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public UserService(FileStorageService fileStorageService, PasswordEncoder passwordEncoder) {
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAll() {
        return fileStorageService.readList(FILENAME, User.class);
    }

    public Optional<User> findByUsername(String username) {
        return getAll().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    public boolean existsByUsername(String username) {
        return getAll().stream().anyMatch(u -> u.getUsername().equals(username));
    }

    public User register(String username, String password, String email, String firstName, String lastName) {
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role("ROLE_USER")
                .build();
        List<User> users = getAll();
        users.add(user);
        fileStorageService.writeList(FILENAME, users);
        return user;
    }

    public User save(User user) {
        List<User> users = getAll();
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
            users.add(user);
        } else {
            boolean found = users.stream().anyMatch(u -> u.getId().equals(user.getId()));
            if (found) {
                users.replaceAll(u -> u.getId().equals(user.getId()) ? user : u);
            } else {
                users.add(user);
            }
        }
        fileStorageService.writeList(FILENAME, users);
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Uživatel nenalezen: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
