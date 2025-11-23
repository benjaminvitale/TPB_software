package org.udesa.giftcards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class MerchantVault extends ModelEntity {

    @Column(unique = true)
    private String merchantCode; // Ej: "M1"

    public MerchantVault(String merchantCode) {
        this.merchantCode = merchantCode;
    }
}