package org.udesa.giftcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.udesa.giftcards.model.GiftCard;
import java.util.Optional;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    Optional<GiftCard> findBySerialNumber(String serialNumber);
}