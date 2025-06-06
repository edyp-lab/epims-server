package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * TreatmentsApplication generated by hbm2java
 */
@Entity
@Table(name = "treatments_application", schema = "public")
public class TreatmentsApplication implements java.io.Serializable {

	private TreatmentsApplicationId id;
	private ProtocolApplication protocolApplication;
	private Treatments treatments;
	private Integer rank;

	public TreatmentsApplication() {
	}

	public TreatmentsApplication(TreatmentsApplicationId id, ProtocolApplication protocolApplication,
			Treatments treatments) {
		this.id = id;
		this.protocolApplication = protocolApplication;
		this.treatments = treatments;
	}

	public TreatmentsApplication(TreatmentsApplicationId id, ProtocolApplication protocolApplication,
			Treatments treatments, Integer rank) {
		this.id = id;
		this.protocolApplication = protocolApplication;
		this.treatments = treatments;
		this.rank = rank;
	}

	@EmbeddedId

	@AttributeOverrides({
			@AttributeOverride(name = "treatments", column = @Column(name = "treatments", nullable = false)),
			@AttributeOverride(name = "protocolApplication", column = @Column(name = "protocol_application", nullable = false)) })
	public TreatmentsApplicationId getId() {
		return this.id;
	}

	public void setId(TreatmentsApplicationId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "protocol_application", nullable = false, insertable = false, updatable = false)
	public ProtocolApplication getProtocolApplication() {
		return this.protocolApplication;
	}

	public void setProtocolApplication(ProtocolApplication protocolApplication) {
		this.protocolApplication = protocolApplication;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "treatments", nullable = false, insertable = false, updatable = false)
	public Treatments getTreatments() {
		return this.treatments;
	}

	public void setTreatments(Treatments treatments) {
		this.treatments = treatments;
	}

	@Column(name = "rank")
	public Integer getRank() {
		return this.rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

}
