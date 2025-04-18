package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * Lane generated by hbm2java
 */
@Entity
@Table(name = "lane", schema = "public")
public class Lane implements java.io.Serializable {

	private int id;
	private Gel1d gel1d;
	private SeparationResult separationResult;
	private String picture;

	public Lane() {
	}

	public Lane(SeparationResult separationResult) {
		this.separationResult = separationResult;
	}

	public Lane(Gel1d gel1d, SeparationResult separationResult, String picture) {
		this.gel1d = gel1d;
		this.separationResult = separationResult;
		this.picture = picture;
	}

	@GenericGenerator(name = "org.o7planning.generateentities.LaneIdGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "separationResult"))
	@Id
	@GeneratedValue(generator = "org.o7planning.generateentities.LaneIdGenerator")

	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gel_1d")
	public Gel1d getGel1d() {
		return this.gel1d;
	}

	public void setGel1d(Gel1d gel1d) {
		this.gel1d = gel1d;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	public SeparationResult getSeparationResult() {
		return this.separationResult;
	}

	public void setSeparationResult(SeparationResult separationResult) {
		this.separationResult = separationResult;
	}

	@Column(name = "picture")
	public String getPicture() {
		return this.picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

}
