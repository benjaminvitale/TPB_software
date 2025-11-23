package org.udesa.giftcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.udesa.giftcards.model.UserVault;
import java.util.Optional;

public interface UserVaultRepository extends JpaRepository<UserVault, Long> {
    // CORRECCIÓN: findByName -> findByUsername
    Optional<UserVault> findByUsername(String username);
}
