package org.udesa.giftcards.model;

import org.springframework.stereotype.Service;
import org.udesa.giftcards.repository.GiftCardRepository;


@Service
public class GiftCardService extends ModelService<GiftCard, GiftCardRepository> {

    @Override
    protected void updateData(GiftCard existing, GiftCard updated) {

    }

    public GiftCard findBySerial(String serial) {
        return repository.findBySerialNumber(serial)
                .orElseThrow(() -> new RuntimeException("CardNotFound"));
    }
}
