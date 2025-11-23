package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GiftCardTest {

    @Test
    public void aSimpleCard() {

        assertEquals( 10, newCard().getBalance() );
    }

    @Test
    public void aSimpleIsNotOwnedCard() {

        assertFalse( newCard().isOwned() );
    }

    @Test
    public void cannotChargeUnownedCards() {
        GiftCard aCard = newCard();

        // La excepción es la misma, la lógica de negocio se mantiene
        assertThrows( RuntimeException.class, () -> aCard.charge( 2, "Un cargo" ) );

        assertEquals( 10, aCard.getBalance() );
        assertTrue( aCard.getCharges().isEmpty() );
    }

    @Test
    public void chargeACard() {
        GiftCard aCard = newCard();
        aCard.redeem( "Bob" ); // redeem sigue igual
        aCard.charge( 2, "Un cargo" );

        assertEquals( 8, aCard.getBalance() );

        assertEquals( "Un cargo", aCard.getCharges().get(aCard.getCharges().size() - 1) );
    }

    @Test
    public void cannotOverrunACard() {
        GiftCard aCard = newCard();
        assertThrows( RuntimeException.class, () -> aCard.charge( 11, "Un cargo" ) );
        assertEquals( 10, aCard.getBalance() );
    }

    private GiftCard newCard() {
        return new GiftCard( "GC1", 10 );
    }

}
