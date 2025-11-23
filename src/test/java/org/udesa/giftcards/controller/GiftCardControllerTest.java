package org.udesa.giftcards.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.udesa.giftcards.model.Clock;
import org.udesa.giftcards.model.GiftCard;
import org.udesa.giftcards.model.MerchantVault;
import org.udesa.giftcards.model.UserVault;
import org.udesa.giftcards.repository.GiftCardRepository;
import org.udesa.giftcards.repository.MerchantVaultRepository;
import org.udesa.giftcards.repository.UserVaultRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class GiftCardControllerTest {

    public static Random randomStream = new Random(Instant.now().getEpochSecond());

    @Autowired MockMvc mockMvc;
    @Autowired UserVaultRepository userRepo;
    @Autowired GiftCardRepository cardRepo;
    @Autowired MerchantVaultRepository merchantRepo;

    @MockBean Clock clock;

    @BeforeEach
    public void beforeEach() {
        // Fijamos el tiempo para que no varíe durante la ejecución, salvo que lo cambiemos explícitamente
        when(clock.now()).then(it -> LocalDateTime.now());
    }

    // --- SETUP HELPERS ---
    private UserVault savedUser() {
        return userRepo.save(new UserVault("User" + nextKey(), "Pass" + nextKey()));
    }

    private GiftCard savedCard(int balance) {
        return cardRepo.save(new GiftCard("GC" + nextKey(), balance));
    }

    private MerchantVault savedMerchant() {
        return merchantRepo.save(new MerchantVault("M" + nextKey()));
    }

    private int nextKey() {
        return Math.abs(randomStream.nextInt());
    }

    // --- TESTS ---

    @Test
    public void test01CanLoginWithValidUser() throws Exception {
        UserVault user = savedUser();
        String token = login(user.getUsername(), user.getPassword());
        assertNotNull(token);
    }

    @Test
    public void test02CannotLoginWithInvalidUser() throws Exception {
        loginFailing("InvalidUser", "Pass");
    }

    @Test
    public void test03CannotLoginWithInvalidPassword() throws Exception {
        UserVault user = savedUser();
        loginFailing(user.getUsername(), "WrongPass");
    }

    @Test
    public void test04CanRedeemACard() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard(100);
        String token = login(user.getUsername(), user.getPassword());

        redeem(token, card.getSerialNumber());

        // Verificamos que el saldo sea consultable
        assertEquals(100, balance(token, card.getSerialNumber()));
    }

    @Test
    public void test05CannotRedeemAlreadyOwnedCard() throws Exception {
        UserVault user1 = savedUser();
        UserVault user2 = savedUser();
        GiftCard card = savedCard(100);

        String token1 = login(user1.getUsername(), user1.getPassword());
        redeem(token1, card.getSerialNumber());

        String token2 = login(user2.getUsername(), user2.getPassword());
        redeemFailing(token2, card.getSerialNumber()); // Fallo esperado
    }

    @Test
    public void test06CanCheckBalanceOfOwnedCard() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard(500);
        String token = login(user.getUsername(), user.getPassword());

        redeem(token, card.getSerialNumber());

        assertEquals(500, balance(token, card.getSerialNumber()));
    }

    @Test
    public void test07CannotCheckBalanceOfNotOwnedCard() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard(500); // Tarjeta virgen
        String token = login(user.getUsername(), user.getPassword());

        balanceFailing(token, card.getSerialNumber());
    }

    @Test
    public void test08MerchantCanChargeACard() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard(100);
        MerchantVault merchant = savedMerchant();
        String token = login(user.getUsername(), user.getPassword());

        redeem(token, card.getSerialNumber());

        charge(merchant.getMerchantCode(), card.getSerialNumber(), 20, "Cafe");

        assertEquals(80, balance(token, card.getSerialNumber()));
    }

    @Test
    public void test09MerchantCannotChargeMoreThanBalance() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard(10);
        MerchantVault merchant = savedMerchant();
        String token = login(user.getUsername(), user.getPassword());

        redeem(token, card.getSerialNumber());

        chargeFailing(merchant.getMerchantCode(), card.getSerialNumber(), 20, "Caro");
    }

    @Test
    public void test10CannotChargeInvalidMerchant() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard(100);
        String token = login(user.getUsername(), user.getPassword());

        redeem(token, card.getSerialNumber());

        chargeFailing("FakeMerchant", card.getSerialNumber(), 10, "Robo");
    }

    @Test
    public void test11DetailsShowCharges() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard(100);
        MerchantVault merchant = savedMerchant();
        String token = login(user.getUsername(), user.getPassword());

        redeem(token, card.getSerialNumber());
        charge(merchant.getMerchantCode(), card.getSerialNumber(), 10, "Item1");
        charge(merchant.getMerchantCode(), card.getSerialNumber(), 20, "Item2");

        List<String> details = details(token, card.getSerialNumber());
        assertEquals(2, details.size());
        assertTrue(details.contains("Item1"));
        assertTrue(details.contains("Item2"));
    }

    @Test
    public void test12TokenExpiresAfterTime() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard(100);

        // 1. "Congelamos" el tiempo en una variable fija
        LocalDateTime fixedStartTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

        // Forzamos al reloj a devolver esa hora exacta para el Login
        when(clock.now()).thenReturn(fixedStartTime);

        String token = login(user.getUsername(), user.getPassword());

        // Redimimos OK (seguimos en el mismo instante)
        redeem(token, card.getSerialNumber());

        // 2. VIAJE EN EL TIEMPO: Forzamos al reloj a devolver 6 minutos después
        // La sesión se creó a las 12:00. Ahora son las 12:06. Límite: 5 min.
        when(clock.now()).thenReturn(fixedStartTime.plusMinutes(6));

        // 3. Intentamos consultar saldo -> Debe fallar (500) porque el token expiró
        balanceFailing(token, card.getSerialNumber());
    }

    // --- HTTP REQUEST HELPERS (Estilo TusLibros) ---

    private String login(String user, String pass) throws Exception {
        String response = mockMvc.perform(post("/api/giftcards/login")
                        .param("user", user)
                        .param("pass", pass))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, String> map = new ObjectMapper().readValue(response, Map.class);
        return map.get("token");
    }

    private void loginFailing(String user, String pass) throws Exception {
        mockMvc.perform(post("/api/giftcards/login")
                        .param("user", user)
                        .param("pass", pass))
                .andDo(print())
                .andExpect(status().is(500)); // Esperamos 500 según el Controller
    }

    private void redeem(String token, String cardId) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/redeem")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private void redeemFailing(String token, String cardId) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/redeem")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(500));
    }

    private int balance(String token, String cardId) throws Exception {
        String response = mockMvc.perform(get("/api/giftcards/" + cardId + "/balance")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Integer> map = new ObjectMapper().readValue(response, Map.class);
        return map.get("balance");
    }

    private void balanceFailing(String token, String cardId) throws Exception {
        mockMvc.perform(get("/api/giftcards/" + cardId + "/balance")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(500));
    }

    private void charge(String merchant, String cardId, int amount, String description) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/charge")
                        .param("merchant", merchant)
                        .param("amount", String.valueOf(amount))
                        .param("description", description))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private void chargeFailing(String merchant, String cardId, int amount, String description) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/charge")
                        .param("merchant", merchant)
                        .param("amount", String.valueOf(amount))
                        .param("description", description))
                .andDo(print())
                .andExpect(status().is(500));
    }

    private List<String> details(String token, String cardId) throws Exception {
        String response = mockMvc.perform(get("/api/giftcards/" + cardId + "/details")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, List<String>> map = new ObjectMapper().readValue(response, Map.class);
        return map.get("details");
    }
}
