package org.udesa.giftcards.model;

import org.springframework.stereotype.Service;
import org.udesa.giftcards.repository.MerchantVaultRepository;

@Service
public class MerchantVaultService extends ModelService<MerchantVault, MerchantVaultRepository> {

    @Override
    protected void updateData(MerchantVault existing, MerchantVault updated) {
        // El código NO se toca (es identidad), pero el nombre sí puede cambiar
        existing.setName(updated.getName());
    }

    public boolean exists(String code) {
        return repository.findByMerchantCode(code).isPresent();
    }
}