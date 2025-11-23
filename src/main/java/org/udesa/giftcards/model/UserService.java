package org.udesa.giftcards.model;

import org.springframework.stereotype.Service;
import org.udesa.giftcards.repository.UserVaultRepository;

@Service
public class UserService extends ModelService<UserVault, UserVaultRepository> {

    @Override
    protected void updateData(UserVault existing, UserVault updated) {
        existing.setUsername(updated.getUsername());
        existing.setPassword(updated.getPassword());
    }

    public UserVault findByName(String username) {
        // CORRECCIÓN: Llamamos al nuevo método findByUsername
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(GifCardFacade.InvalidUser));
    }
}
