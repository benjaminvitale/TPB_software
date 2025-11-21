package org.udesa.giftcards.model;

import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    public static final String CargoImposible = "CargoImposible";
    public static final String InvalidCard = "InvalidCard";
    private String id;
    private int balance;
    private String owner;
    private List<String> charges = new ArrayList<>();

    public GiftCard( String id, int initialBalance ) {
        this.id = id;
        balance = initialBalance;
    }

    public GiftCard charge( int anAmount, String description ) {
        if ( !owned() || ( balance - anAmount < 0 ) ) throw new RuntimeException( CargoImposible );

        balance = balance - anAmount;
        charges.add( description );

        return this;
    }

    public GiftCard redeem( String newOwner ) {
        if ( owned() ) throw new RuntimeException( InvalidCard );

        owner = newOwner;
        return this;
    }

    // proyectors
    public boolean owned() {                            return owner != null;                   }
    public boolean isOwnedBy( String aPossibleOwner ) { return owner.equals( aPossibleOwner );  }

    // accessors
    public String id() {            return id;      }
    public int balance() {          return balance; }
    public List<String> charges() { return charges; }

}
