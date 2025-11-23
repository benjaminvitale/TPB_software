package org.udesa.giftcards.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.udesa.giftcards.model.*;
import org.udesa.giftcards.repository.*;

@Configuration
public class GiftCardConfig {

    @Bean
    public Clock clock() {
        return new Clock();
    }

    // El "Seeder" de la Base de Datos
    @Bean
    public CommandLineRunner initData(
            UserVaultRepository userRepo,
            MerchantRepository merchantRepo,
            GiftCardRepository cardRepo) {

        return args -> {
            // 1. Crear Usuarios (UserVaults)
            if (userRepo.count() == 0) {
                userRepo.save(new UserVault("pepe", "1234")); // Usuario de prueba
                userRepo.save(new UserVault("maria", "password"));
                System.out.println(">>> Usuarios cargados.");
            }

            // 2. Crear Merchants
            if (merchantRepo.count() == 0) {
                merchantRepo.save(new Merchant("M1")); // El merchant del test
                merchantRepo.save(new Merchant("ZARA"));
                System.out.println(">>> Merchants cargados.");
            }

            // 3. Crear GiftCards
            if (cardRepo.count() == 0) {
                // Tarjetas vírgenes (sin dueño)
                cardRepo.save(new GiftCard("GC1", 1000));
                cardRepo.save(new GiftCard("GC2", 500));
                System.out.println(">>> GiftCards cargadas.");
            }
        };
    }
}