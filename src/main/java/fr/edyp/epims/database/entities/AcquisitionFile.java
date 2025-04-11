package fr.edyp.epims.database.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("acquisition")
public class AcquisitionFile extends FileLink implements java.io.Serializable {


    private ProtocolApplication protocolApplication;


    public AcquisitionFile() {
    }


    public ProtocolApplication getProtocolApplication() {
        return this.protocolApplication;
    }

    public void setProtocolApplication(ProtocolApplication protocolApplication) {
        this.protocolApplication = protocolApplication;
    }

}
