package org.udesa.giftcards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class UserVault extends ModelEntity {

    @Column(unique = true)
    private String username;

    private String password;

    public UserVault(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
