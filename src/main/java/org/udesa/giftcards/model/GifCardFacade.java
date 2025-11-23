
package org.udesa.giftcards.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class GifCardFacade {

    public static final String InvalidUser = "InvalidUser";
    public static final String InvalidMerchant = "InvalidMerchant";
    public static final String InvalidToken = "InvalidToken";

    // Inyectamos los SERVICIOS, no los repositorios directos (Estilo TusLibros)
    @Autowired private UserService userService;
    @Autowired private GiftCardService cardService;
    @Autowired private MerchantService merchantService;
    @Autowired private Clock clock;

    // Sesiones en memoria (Tokens) - Esto no cambia
    private Map<UUID, UserSession> sessions = new HashMap<>();

    public UUID login(String username, String pass) {
        // Verifica contra UserVault
        UserVault user = userService.findByName(username);

        if (!user.getPassword().equals(pass)) {
            throw new RuntimeException(InvalidUser);
        }

        UUID token = UUID.randomUUID();
        sessions.put(token, new UserSession(username, clock));
        return token;
    }

    public void redeem(UUID token, String serialNumber) {
        String username = findUser(token); // Valida token
        GiftCard card = cardService.findBySerial(serialNumber); // Busca GiftCard

        card.redeem(username); // Lógica de dominio

        cardService.add(card); // Persiste cambios usando el método 'add' de ModelService
    }

    public int balance(UUID token, String serialNumber) {
        return ownedCard(token, serialNumber).getBalance();
    }

    public List<String> details(UUID token, String serialNumber) {
        return ownedCard(token, serialNumber).getCharges();
    }

    public void charge(String merchantCode, String serialNumber, int amount, String description) {
        if (!merchantService.exists(merchantCode)) {
            throw new RuntimeException(InvalidMerchant);
        }

        GiftCard card = cardService.findBySerial(serialNumber);
        card.charge(amount, description);
        cardService.add(card); // Persiste
    }

    // Helpers privados
    private GiftCard ownedCard(UUID token, String serialNumber) {
        String username = findUser(token);
        GiftCard card = cardService.findBySerial(serialNumber);
        if (!card.isOwnedBy(username)) throw new RuntimeException(InvalidToken);
        return card;
    }

    private String findUser(UUID token) {
        return sessions.computeIfAbsent(token, k -> { throw new RuntimeException(InvalidToken); })
                .userAliveAt(clock);
    }
}