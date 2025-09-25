package br.com.hyperativa.api.config;

import br.com.hyperativa.api.model.entity.Role;
import br.com.hyperativa.api.model.entity.User;
import br.com.hyperativa.api.model.enums.RoleEnum;
import br.com.hyperativa.api.repository.RoleRepository;
import br.com.hyperativa.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Verificando e criando dados iniciais (roles e usuários)...");
        Role userRole = createRoleIfNotFound(RoleEnum.ROLE_USER);
        Role adminRole = createRoleIfNotFound(RoleEnum.ROLE_ADMIN);
        createUserIfNotFound("admin", "admin_password", Set.of(adminRole));
        createUserIfNotFound("testuser", "password", Set.of(userRole));
        log.info("Inicializacao de dados concluida.");
    }

    private Role createRoleIfNotFound(RoleEnum roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    log.info("Criando role: {}", roleName);
                    return roleRepository.save(new Role(roleName));
                });
    }

    private void createUserIfNotFound(String username, String password, Set<Role> roles) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .roles(roles)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("Usuario '{}' criado com sucesso.", username);
        } else {
            log.info("Usuario '{}' ja existe.", username);
        }
    }
}