package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class GifCardFacadeTest {
    // Se espera que el usuario pueda inciar sesion con usuario y password y obtener un token
    //    debe poder usar el token para gestionar la tarjeta.
    //    el token se vence a los 5'

    // las giftcards ya estan definidas en el sistema.
    //    el usuario las reclama, pueden ser varias
    //    puede consultar el saldo y el detalle de gastos de sus tarjetas

    // los merchants pueden hacer cargos en las tarjetas que hayan sido reclamadas.
    //    los cargos se actualizan en el balance de las tarjetas


    @Test public void userCanOpenASession() {
        assertNotNull( newFacade().login( "Bob", "BobPass" ) );
    }

    @Test public void unkownUserCannorOpenASession() {
        assertThrows( RuntimeException.class, () -> newFacade().login( "Stuart", "StuPass" ) );
    }

    @Test public void userCannotUseAnInvalidtoken() {
        assertThrows( RuntimeException.class, () -> newFacade().redeem( UUID.randomUUID(), "GC1" ) );
        assertThrows( RuntimeException.class, () -> newFacade().balance( UUID.randomUUID(), "GC1" ) );
        assertThrows( RuntimeException.class, () -> newFacade().details( UUID.randomUUID(), "GC1" ) );
    }

    @Test public void userCannotCheckOnAlienCard() {
        GifCardFacade facade = newFacade();
        UUID token = facade.login( "Bob", "BobPass" );

        assertThrows( RuntimeException.class, () -> facade.balance( token, "GC1" ) );
    }

    @Test public void userCanRedeeemACard() {
        GifCardFacade facade = newFacade();
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        assertEquals( 10, facade.balance( token, "GC1" ) );
    }

    @Test public void userCanRedeeemASecondCard() {
        GifCardFacade facade = newFacade();
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        facade.redeem( token, "GC2" );

        assertEquals( 10, facade.balance( token, "GC1" ) );
        assertEquals( 5, facade.balance( token, "GC2" ) );
    }

    @Test public void multipleUsersCanRedeeemACard() {
        GifCardFacade facade = newFacade();
        UUID bobsToken = facade.login( "Bob", "BobPass" );
        UUID kevinsToken = facade.login( "Kevin", "KevPass" );

        facade.redeem( bobsToken, "GC1" );
        facade.redeem( kevinsToken, "GC2" );

        assertEquals( 10, facade.balance( bobsToken, "GC1" ) );
        assertEquals( 5, facade.balance( kevinsToken, "GC2" ) );
    }

    @Test public void unknownMerchantCantCharge() {
        assertThrows( RuntimeException.class, () -> newFacade().charge( "Mx", "GC1", 2, "UnCargo" ) );

    }

    @Test public void merchantCantChargeUnredeemedCard() {
        assertThrows( RuntimeException.class, () -> newFacade().charge( "M1", "GC1", 2, "UnCargo" ) );
    }

    @Test public void merchantCanChargeARedeemedCard() {
        GifCardFacade facade = newFacade();
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        facade.charge( "M1", "GC1", 2, "UnCargo" );

        assertEquals( 8, facade.balance( token, "GC1" ) );
    }

    @Test public void merchantCannotOverchargeACard() {
        GifCardFacade facade = newFacade();
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        assertThrows( RuntimeException.class, () -> facade.charge( "M1", "GC1", 11, "UnCargo" ) );
    }

    @Test public void userCanCheckHisEmptyCharges() {
        GifCardFacade facade = newFacade();
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );

        assertTrue( facade.details( token, "GC1" ).isEmpty() );
    }

    @Test public void userCanCheckHisCharges() {
        GifCardFacade facade = newFacade();
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        facade.charge( "M1", "GC1", 2, "UnCargo" );

        assertEquals( "UnCargo", facade.details( token, "GC1" ).getLast() );
    }

    @Test public void userCannotCheckOthersCharges() {
        GifCardFacade facade = newFacade();
        facade.redeem( facade.login( "Bob", "BobPass" ), "GC1" );

        UUID token = facade.login( "Kevin", "KevPass" );

        assertThrows( RuntimeException.class, () -> facade.details( token, "GC1" ) );
    }

    @Test public void tokenExpires() {
        GifCardFacade facade = newFacade( new Clock( ){
            Iterator<LocalDateTime> it = List.of( LocalDateTime.now(), LocalDateTime.now().plusMinutes( 16 ) ).iterator();
            public LocalDateTime now() {
                return it.next();
            }
        } );

        UUID token = facade.login( "Kevin", "KevPass" );

        assertThrows( RuntimeException.class, () -> facade.redeem( token, "GC1" ) );
    }

    private static GifCardFacade newFacade() {return newFacade( new Clock() );    }
    private static GifCardFacade newFacade( Clock  clock ) {
        return new GifCardFacade( new ArrayList( List.of( new GiftCard( "GC1", 10 ), new GiftCard( "GC2", 5 ) ) ),
                                  new HashMap( Map.of( "Bob", "BobPass", "Kevin", "KevPass" ) ),
                                  new ArrayList<>( List.of( "M1" ) ),
                                  clock );
    }

}
