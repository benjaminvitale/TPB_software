package org.udesa.giftcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.udesa.giftcards.model.*;
import java.util.Optional;

// Para UserVault
public interface UserVaultRepository extends JpaRepository<UserVault, Long> {
    Optional<UserVault> findByName(String name);
}

// Para GiftCard (sin Vault)
public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    Optional<GiftCard> findBySerialNumber(String serialNumber);
}

// Para Merchant (sin Vault)
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByCode(String code);
}