package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito
public class GifCardFacadeTest {

    @Mock private UserService userService;
    @Mock private GiftCardService cardService;
    @Mock private MerchantVaultService merchantService;
    @Mock private Clock clock;

    @InjectMocks
    private GifCardFacade facade;

    private UserVault bob;
    private GiftCard card1;
    private GiftCard card2;

    @BeforeEach
    public void setUp() {
        // Datos de prueba comunes
        bob = new UserVault("Bob", "BobPass");
        card1 = new GiftCard("GC1", 10);
        card2 = new GiftCard("GC2", 5);

        // Configuramos el reloj por defecto
        lenient().when(clock.now()).thenReturn(LocalDateTime.now());
    }

    @Test
    public void userCanOpenASession() {
        when(userService.findByName("Bob")).thenReturn(bob);
        assertNotNull(facade.login("Bob", "BobPass"));
    }

    @Test
    public void unknownUserCannotOpenASession() {
        // Simulamos que el servicio lanza error si no encuentra al user
        when(userService.findByName("Stuart")).thenThrow(new RuntimeException(GifCardFacade.InvalidUser));
        assertThrows(RuntimeException.class, () -> facade.login("Stuart", "StuPass"));
    }

    @Test
    public void userCannotUseAnInvalidToken() {
        assertThrows(RuntimeException.class, () -> facade.redeem(UUID.randomUUID(), "GC1"));
    }

    @Test
    public void userCannotCheckOnAlienCard() {
        // Login exitoso
        when(userService.findByName("Bob")).thenReturn(bob);
        UUID token = facade.login("Bob", "BobPass");

        // Simulamos que la tarjeta existe pero NO es de Bob (ownerUsername es null o de otro)
        when(cardService.findBySerial("GC1")).thenReturn(card1);

        assertThrows(RuntimeException.class, () -> facade.balance(token, "GC1"));
    }

    @Test
    public void userCanRedeemACard() {
        when(userService.findByName("Bob")).thenReturn(bob);
        UUID token = facade.login("Bob", "BobPass");

        when(cardService.findBySerial("GC1")).thenReturn(card1);

        facade.redeem(token, "GC1");

        // Verificamos que se haya llamado a 'redeem' en el objeto y guardado
        assertEquals("Bob", card1.getOwnerUsername());
        verify(cardService).add(card1); // Verificamos persistencia
    }

    @Test
    public void merchantCanChargeARedeemedCard() {
        // 1. Login y Redeem
        when(userService.findByName("Bob")).thenReturn(bob);
        UUID token = facade.login("Bob", "BobPass");

        // Simulamos la tarjeta ya redimida por Bob
        card1.setOwnerUsername("Bob");
        when(cardService.findBySerial("GC1")).thenReturn(card1);

        // Simulamos que el merchant existe
        when(merchantService.exists("M1")).thenReturn(true);

        // 2. Charge
        facade.charge("M1", "GC1", 2, "UnCargo");

        assertEquals(8, card1.getBalance());
        verify(cardService).add(card1);
    }

    @Test
    public void merchantCannotOverchargeACard() {
        card1.setOwnerUsername("Bob");
        when(cardService.findBySerial("GC1")).thenReturn(card1);
        when(merchantService.exists("M1")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> facade.charge("M1", "GC1", 11, "UnCargo"));
    }

    @Test
    public void tokenExpires() {
        when(userService.findByName("Kevin")).thenReturn(new UserVault("Kevin", "KevPass"));

        // Manipulamos el tiempo: Primero es AHORA, luego es AHORA + 6 min
        LocalDateTime now = LocalDateTime.now();
        when(clock.now())
                .thenReturn(now) // Al momento del login
                .thenReturn(now.plusMinutes(6)); // Al momento del redeem (Expirado > 5 min)

        UUID token = facade.login("Kevin", "KevPass");

        assertThrows(RuntimeException.class, () -> facade.redeem(token, "GC1"));
    }
}