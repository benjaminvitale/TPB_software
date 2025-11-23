package org.udesa.giftcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.udesa.giftcards.model.MerchantVault;
import java.util.Optional;

public interface MerchantVaultRepository extends JpaRepository<MerchantVault, Long> {
    Optional<MerchantVault> findByMerchantCode(String merchantCode);
}