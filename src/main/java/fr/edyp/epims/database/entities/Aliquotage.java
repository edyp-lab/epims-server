package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * Aliquotage generated by hbm2java
 */
@Entity
@Table(name = "aliquotage", schema = "public")
public class Aliquotage implements java.io.Serializable {

	private int id;
	private Separation separation;
	private Integer subSampleCount;

	public Aliquotage() {
	}

	public Aliquotage(Separation separation) {
		this.separation = separation;
	}

	public Aliquotage(Separation separation, Integer subSampleCount) {
		this.separation = separation;
		this.subSampleCount = subSampleCount;
	}

	@GenericGenerator(name = "org.o7planning.generateentities.AliquotageIdGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "separation"))
	@Id
	@GeneratedValue(generator = "org.o7planning.generateentities.AliquotageIdGenerator")

	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	public Separation getSeparation() {
		return this.separation;
	}

	public void setSeparation(Separation separation) {
		this.separation = separation;
	}

	@Column(name = "sub_sample_count")
	public Integer getSubSampleCount() {
		return this.subSampleCount;
	}

	public void setSubSampleCount(Integer subSampleCount) {
		this.subSampleCount = subSampleCount;
	}

}
