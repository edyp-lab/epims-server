package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * MassPlanning generated by hbm2java
 */
@Entity
@Table(name = "mass_planning", schema = "public")
public class MassPlanning implements java.io.Serializable {

	private int id;
	private Actor actor;
	private Sample sample;
	private String instrumentModel;
	private String date;
	private Short injectionCount;
	private String description;
	private String sampleConsumed;
	private String name;

	public MassPlanning() {
	}

	public MassPlanning(int id, Sample sample) {
		this.id = id;
		this.sample = sample;
	}

	public MassPlanning(int id, Actor actor, Sample sample, String instrumentModel, String date,
			Short injectionCount, String description, String sampleConsumed, String name) {
		this.id = id;
		this.actor = actor;
		this.sample = sample;
		this.instrumentModel = instrumentModel;
		this.date = date;
		this.injectionCount = injectionCount;
		this.description = description;
		this.sampleConsumed = sampleConsumed;
		this.name = name;
	}

	@Id

	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor")
	public Actor getActor() {
		return this.actor;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sample", nullable = false)
	public Sample getSample() {
		return this.sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	@Column(name = "instrument_model")
	public String getInstrumentModel() {
		return this.instrumentModel;
	}

	public void setInstrumentModel(String instrumentModel) {
		this.instrumentModel = instrumentModel;
	}

	@Column(name = "date")
	public String getDate() {
		return this.date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Column(name = "injection_count")
	public Short getInjectionCount() {
		return this.injectionCount;
	}

	public void setInjectionCount(Short injectionCount) {
		this.injectionCount = injectionCount;
	}

	@Column(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "sample_consumed")
	public String getSampleConsumed() {
		return this.sampleConsumed;
	}

	public void setSampleConsumed(String sampleConsumed) {
		this.sampleConsumed = sampleConsumed;
	}

	@Column(name = "name", length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
