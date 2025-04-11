package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * TreatmentsApplicationId generated by hbm2java
 */
@Embeddable
public class TreatmentsApplicationId implements java.io.Serializable {

	private int treatments;
	private int protocolApplication;

	public TreatmentsApplicationId() {
	}

	public TreatmentsApplicationId(int treatments, int protocolApplication) {
		this.treatments = treatments;
		this.protocolApplication = protocolApplication;
	}

	@Column(name = "treatments", nullable = false)
	public int getTreatments() {
		return this.treatments;
	}

	public void setTreatments(int treatments) {
		this.treatments = treatments;
	}

	@Column(name = "protocol_application", nullable = false)
	public int getProtocolApplication() {
		return this.protocolApplication;
	}

	public void setProtocolApplication(int protocolApplication) {
		this.protocolApplication = protocolApplication;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof TreatmentsApplicationId))
			return false;
		TreatmentsApplicationId castOther = (TreatmentsApplicationId) other;

		return (this.getTreatments() == castOther.getTreatments())
				&& (this.getProtocolApplication() == castOther.getProtocolApplication());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getTreatments();
		result = 37 * result + this.getProtocolApplication();
		return result;
	}

}
