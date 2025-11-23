package org.udesa.giftcards.model;

import org.springframework.stereotype.Service;
import org.udesa.giftcards.repository.GiftCardRepository;

// Fíjate: Hereda de ModelService<GiftCard, GiftCardRepository>
@Service
public class GiftCardService extends ModelService<GiftCard, GiftCardRepository> {

    @Override
    protected void updateData(GiftCard existing, GiftCard updated) {
        // Lógica de actualización si fuera necesaria (ej: admins cambiando saldos a mano)
    }

    public GiftCard findBySerial(String serial) {
        return repository.findBySerialNumber(serial)
                .orElseThrow(() -> new RuntimeException("CardNotFound"));
    }
}
