package org.udesa.giftcards.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GifCardFacade {
    public static final String InvalidUser = "InvalidUser";
    public static final String InvalidMerchant = "InvalidMerchant";
    public static final String InvalidToken = "InvalidToken";

    private Map<String, String> users;
    private Map<String,GiftCard> cards;
    private List<String>  merchants;
    private Clock clock;

    private Map<UUID, UserSession> sessions = new HashMap();

    public GifCardFacade( List<GiftCard> cards, Map<String, String> users, List<String> merchants, Clock clock ) {
        this.cards = cards.stream().collect( Collectors.toMap( each -> each.id(), each -> each ));
        this.users = users;
        this.merchants = merchants;
        this.clock = clock;
    }

    public UUID login( String userKey, String pass ) {
        if ( !users.computeIfAbsent( userKey, key -> { throw new RuntimeException( InvalidUser ); } )
                .equals( pass ) ) {
            throw new RuntimeException( InvalidUser );
        }

        UUID token = UUID.randomUUID();
        sessions.put( token, new UserSession( userKey, clock ) );
        return token;
    }

    public void redeem( UUID token, String cardId ) {
        cards.get( cardId ).redeem( findUser( token ) );
    }

    public int balance( UUID token, String cardId ) {
        return ownedCard( token, cardId ).balance();
    }

    public void charge( String merchantKey, String cardId, int amount, String description ) {
        if ( !merchants.contains( merchantKey ) ) throw new RuntimeException( InvalidMerchant );

        cards.get( cardId ).charge( amount, description );
    }

    public List<String> details( UUID token, String cardId ) {
        return ownedCard( token, cardId ).charges();
    }

    private GiftCard ownedCard( UUID token, String cardId ) {
        GiftCard card = cards.get( cardId );
        if ( !card.isOwnedBy( findUser( token ) ) ) throw new RuntimeException( InvalidToken );
        return card;
    }

    private String findUser( UUID token ) {
        return sessions.computeIfAbsent( token, key -> { throw new RuntimeException( InvalidToken ); } )
                       .userAliveAt( clock );
    }
}
