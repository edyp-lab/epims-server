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
 * GelMigration generated by hbm2java
 */
@Entity
@Table(name = "gel_migration", schema = "public")
public class GelMigration implements java.io.Serializable {

	private int id;
	private Separation separation;
	private String massStart;
	private String massEnd;
	private String denaturingAgent;
	private Gel1dMigration gel1dMigration;
	private Gel2dMigration gel2dMigration;

	public GelMigration() {
	}

	public GelMigration(Separation separation) {
		this.separation = separation;
	}

	public GelMigration(Separation separation, String massStart, String massEnd,
			String denaturingAgent, Gel1dMigration gel1dMigration, Gel2dMigration gel2dMigration) {
		this.separation = separation;
		this.massStart = massStart;
		this.massEnd = massEnd;
		this.denaturingAgent = denaturingAgent;
		this.gel1dMigration = gel1dMigration;
		this.gel2dMigration = gel2dMigration;
	}

	@GenericGenerator(name = "org.o7planning.generateentities.GelMigrationIdGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "separation"))
	@Id
	@GeneratedValue(generator = "org.o7planning.generateentities.GelMigrationIdGenerator")

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

	@Column(name = "mass_start")
	public String getMassStart() {
		return this.massStart;
	}

	public void setMassStart(String massStart) {
		this.massStart = massStart;
	}

	@Column(name = "mass_end")
	public String getMassEnd() {
		return this.massEnd;
	}

	public void setMassEnd(String massEnd) {
		this.massEnd = massEnd;
	}

	@Column(name = "denaturing_agent")
	public String getDenaturingAgent() {
		return this.denaturingAgent;
	}

	public void setDenaturingAgent(String denaturingAgent) {
		this.denaturingAgent = denaturingAgent;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "gelMigration")
	public Gel1dMigration getGel1dMigration() {
		return this.gel1dMigration;
	}

	public void setGel1dMigration(Gel1dMigration gel1dMigration) {
		this.gel1dMigration = gel1dMigration;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "gelMigration")
	public Gel2dMigration getGel2dMigration() {
		return this.gel2dMigration;
	}

	public void setGel2dMigration(Gel2dMigration gel2dMigration) {
		this.gel2dMigration = gel2dMigration;
	}

}
