package fr.edyp.epims.database.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(RunRobotFile.ASSOCIATED_ENTITY)
public class RunRobotFile extends FileLink implements java.io.Serializable {

    public static final String ASSOCIATED_ENTITY = "run_robot";

    public RunRobotFile() {
        setAssociatedEntity(ASSOCIATED_ENTITY);
    }

}
