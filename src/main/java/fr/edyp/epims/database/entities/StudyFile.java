package fr.edyp.epims.database.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(StudyFile.ASSOCIATED_ENTITY)
public class StudyFile extends FileLink implements java.io.Serializable {

    public static final String ASSOCIATED_ENTITY = "study";

    public StudyFile() {
        setAssociatedEntity(ASSOCIATED_ENTITY);
    }

}
