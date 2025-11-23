package org.udesa.giftcards.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class ModelEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id protected long id;

    public boolean equals( Object o ) {
        return this == o ||
                o != null && id != 0 &&
                        getClass() == o.getClass() && id == getClass().cast( o ).getId() &&
                        same( o );
    }

    public int hashCode() {
        return Long.hashCode( id );
    }

    protected boolean same( Object o ) { return true; }
}

