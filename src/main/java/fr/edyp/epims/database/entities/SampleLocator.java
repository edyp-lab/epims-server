package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.*;

/**
 * SampleLocator generated by hbm2java
 */
@Entity
@Table(name = "sample_locator", schema = "public")
public class SampleLocator implements java.io.Serializable {

	private int id;
	private Sample sample;
	private SeparationResult separationResult;
	private ChemicalFraction chemicalFraction;
	private GradientFraction gradientFraction;
	private Band band;
	private Spot spot;
	private Aliquot aliquot;
	private LcFraction lcFraction;

	public SampleLocator() {
	}

	public SampleLocator(int id, SeparationResult separationResult) {
		this.id = id;
		this.separationResult = separationResult;
	}

	public SampleLocator(Sample sample, SeparationResult separationResult) {
		this.sample = sample;
		this.separationResult = separationResult;
	}

	public SampleLocator(int id, Sample sample, SeparationResult separationResult, ChemicalFraction chemicalFraction,
			GradientFraction gradientFraction, Band band, Spot spot, Aliquot aliquot, LcFraction lcFraction) {
		this.id = id;
		this.sample = sample;
		this.separationResult = separationResult;
		this.chemicalFraction = chemicalFraction;
		this.gradientFraction = gradientFraction;
		this.band = band;
		this.spot = spot;
		this.aliquot = aliquot;
		this.lcFraction = lcFraction;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "sample_locator_generator")
	@SequenceGenerator(name = "sample_locator_generator", sequenceName = "sample_locator_id_seq", allocationSize = 1)
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sample")
	public Sample getSample() {
		return this.sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "separation_result", nullable = false)
	public SeparationResult getSeparationResult() {
		return this.separationResult;
	}

	public void setSeparationResult(SeparationResult separationResult) {
		this.separationResult = separationResult;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "sampleLocator")
	public ChemicalFraction getChemicalFraction() {
		return this.chemicalFraction;
	}

	public void setChemicalFraction(ChemicalFraction chemicalFraction) {
		this.chemicalFraction = chemicalFraction;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "sampleLocator")
	public GradientFraction getGradientFraction() {
		return this.gradientFraction;
	}

	public void setGradientFraction(GradientFraction gradientFraction) {
		this.gradientFraction = gradientFraction;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "sampleLocator")
	public Band getBand() {
		return this.band;
	}

	public void setBand(Band band) {
		this.band = band;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "sampleLocator")
	public Spot getSpot() {
		return this.spot;
	}

	public void setSpot(Spot spot) {
		this.spot = spot;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "sampleLocator")
	public Aliquot getAliquot() {
		return this.aliquot;
	}

	public void setAliquot(Aliquot aliquot) {
		this.aliquot = aliquot;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "sampleLocator")
	public LcFraction getLcFraction() {
		return this.lcFraction;
	}

	public void setLcFraction(LcFraction lcFraction) {
		this.lcFraction = lcFraction;
	}

}
