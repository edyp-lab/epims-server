package fr.edyp.epims.database.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(SampleFile.ASSOCIATED_ENTITY)
public class SampleFile extends FileLink implements java.io.Serializable {

    public static final String ASSOCIATED_ENTITY = "sample";

    public SampleFile() {
        setAssociatedEntity(ASSOCIATED_ENTITY);
    }

}
