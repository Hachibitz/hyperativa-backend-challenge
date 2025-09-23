package br.com.hyperativa.api.repository;

import br.com.hyperativa.api.model.entity.Role;
import br.com.hyperativa.api.model.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}