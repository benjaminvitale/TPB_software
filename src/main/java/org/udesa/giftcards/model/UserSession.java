package org.udesa.giftcards.model;

import java.time.LocalDateTime;

public class UserSession {
    String user;
    LocalDateTime stamp;

    public UserSession( String user, Clock clock ) {
        this.user = user;
        this.stamp = clock.now();
    }

    public String userAliveAt( Clock clock ) {
        if (clock.now().isAfter( stamp.plusMinutes( 15 ) )) throw new RuntimeException( "InvalidToken" );

        return user;
    }
}
