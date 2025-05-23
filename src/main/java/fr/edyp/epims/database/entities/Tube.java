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
 * Tube generated by hbm2java
 */
@Entity
@Table(name = "tube", schema = "public")
public class Tube implements java.io.Serializable {

	private int id;
	private Support support;

	public Tube() {
	}

	public Tube(Support support) {
		this.support = support;
	}

	@GenericGenerator(name = "org.o7planning.generateentities.TubeIdGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "support"))
	@Id
	@GeneratedValue(generator = "org.o7planning.generateentities.TubeIdGenerator")

	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	public Support getSupport() {
		return this.support;
	}

	public void setSupport(Support support) {
		this.support = support;
	}

}
