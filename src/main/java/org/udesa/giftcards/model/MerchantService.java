package org.udesa.giftcards.model;

import org.springframework.stereotype.Service;
import org.udesa.giftcards.repository.MerchantRepository;

@Service
public class MerchantService extends ModelService<Merchant, MerchantRepository> {

    @Override
    protected void updateData(Merchant existing, Merchant updated) {}

    public boolean exists(String code) {
        return repository.findByCode(code).isPresent();
    }
}