package org.udesa.giftcards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Merchant extends ModelEntity {
    @Column(unique = true)
    private String code; // Ej: "M1"

    public Merchant(String code) {
        this.code = code;
    }
}