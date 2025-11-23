package org.udesa.giftcards.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor // Obligatorio para JPA
public class GiftCard extends ModelEntity { // <--- Hereda ID y equals/hashCode

    public static final String CargoImposible = "CargoImposible";
    public static final String InvalidCard = "InvalidCard";

    @Column(unique = true)
    private String serialNumber; // El ID de negocio (ej: GC1)

    private int balance;

    // Guardamos el nombre del UserVault dueño
    private String ownerUsername;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> charges = new ArrayList<>();

    public GiftCard(String serialNumber, int initialBalance) {
        this.serialNumber = serialNumber;
        this.balance = initialBalance;
    }

    // Lógica de Negocio
    public void charge(int amount, String description) {
        if (!isOwned() || (balance - amount < 0)) throw new RuntimeException(CargoImposible);
        this.balance -= amount;
        this.charges.add(description);
    }

    public void redeem(String newOwner) {
        if (isOwned()) throw new RuntimeException(InvalidCard);
        this.ownerUsername = newOwner;
    }

    public boolean isOwned() { return ownerUsername != null; }

    public boolean isOwnedBy(String username) {
        return ownerUsername != null && ownerUsername.equals(username);
    }
}