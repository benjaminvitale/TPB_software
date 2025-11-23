package org.udesa.giftcards.model;

import org.springframework.stereotype.Service;
import org.udesa.giftcards.repository.UserVaultRepository;

@Service
public class UserService extends ModelService<UserVault, UserVaultRepository> {

    @Override
    protected void updateData(UserVault existing, UserVault updated) {
        existing.setName(updated.getName());
        existing.setPassword(updated.getPassword());
    }

    public UserVault findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException(GifCardFacade.InvalidUser));
    }
}
