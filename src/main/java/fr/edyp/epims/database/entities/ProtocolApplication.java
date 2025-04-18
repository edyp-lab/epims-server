package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * ProtocolApplication generated by hbm2java
 */
@Entity
@Table(name = "protocol_application", schema = "public")
public class ProtocolApplication implements java.io.Serializable {

	private int id;
	private Actor actor;
	private Protocol protocol;
	private String name;

	@Temporal(TemporalType.DATE)
	private Date date;

	private String comment;
	private Separation separation;
	private Acquisition acquisition;
	private Pool pool;
	private Set<SampleFamily> sampleFamilies = new HashSet<SampleFamily>(0);
	private Set<Transfer> transfers = new HashSet<Transfer>(0);
	private Preparation preparation;
	private Set<TreatmentsApplication> treatmentsApplications = new HashSet<TreatmentsApplication>(0);
	private RunRobot runRobot;

	public ProtocolApplication() {
	}

	public ProtocolApplication(int id, Date date) {
		this.id = id;
		this.date = date;
	}

	public ProtocolApplication(Actor actor, Protocol protocol, String name, Date date,
							   String comment, Separation separation, Acquisition acquisition, Pool pool,
							   Set<SampleFamily> sampleFamilies, Set<Transfer> transfers, Preparation preparation,
							   Set<TreatmentsApplication> treatmentsApplications, RunRobot runRobot) {
		this.actor = actor;
		this.protocol = protocol;
		this.name = name;
		this.date = date;
		this.comment = comment;
		this.separation = separation;
		this.acquisition = acquisition;
		this.pool = pool;
		this.sampleFamilies = sampleFamilies;
		this.transfers = transfers;
		this.preparation = preparation;
		this.treatmentsApplications = treatmentsApplications;
		this.runRobot = runRobot;
	}

	public ProtocolApplication(int id, Actor actor, Protocol protocol, String name, Date date,
			String comment, Separation separation, Acquisition acquisition, Pool pool,
			Set<SampleFamily> sampleFamilies, Set<Transfer> transfers, Preparation preparation,
			Set<TreatmentsApplication> treatmentsApplications, RunRobot runRobot) {
		this.id = id;
		this.actor = actor;
		this.protocol = protocol;
		this.name = name;
		this.date = date;
		this.comment = comment;
		this.separation = separation;
		this.acquisition = acquisition;
		this.pool = pool;
		this.sampleFamilies = sampleFamilies;
		this.transfers = transfers;
		this.preparation = preparation;
		this.treatmentsApplications = treatmentsApplications;
		this.runRobot = runRobot;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "protocol_application_generator")
	@SequenceGenerator(name = "protocol_application_generator", sequenceName = "protocol_application_id_seq", allocationSize = 1)
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
	@JoinColumn(name = "protocol")
	public Protocol getProtocol() {
		return this.protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	@Column(name = "name", length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "date", nullable = false)
	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Column(name = "comment")
	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "protocolApplication")
	public Separation getSeparation() {
		return this.separation;
	}

	public void setSeparation(Separation separation) {
		this.separation = separation;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "protocolApplication")
	public Acquisition getAcquisition() {
		return this.acquisition;
	}

	public void setAcquisition(Acquisition acquisition) {
		this.acquisition = acquisition;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "protocolApplication")
	public Pool getPool() {
		return this.pool;
	}

	public void setPool(Pool pool) {
		this.pool = pool;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "protocolApplication")
	public Set<SampleFamily> getSampleFamilies() {
		return this.sampleFamilies;
	}

	public void setSampleFamilies(Set<SampleFamily> sampleFamilies) {
		this.sampleFamilies = sampleFamilies;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "protocolApplication")
	public Set<Transfer> getTransfers() {
		return this.transfers;
	}

	public void setTransfers(Set<Transfer> transfers) {
		this.transfers = transfers;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "protocolApplication")
	public Preparation getPreparation() {
		return this.preparation;
	}

	public void setPreparation(Preparation preparation) {
		this.preparation = preparation;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "protocolApplication")
	public Set<TreatmentsApplication> getTreatmentsApplications() {
		return this.treatmentsApplications;
	}

	public void setTreatmentsApplications(Set<TreatmentsApplication> treatmentsApplications) {
		this.treatmentsApplications = treatmentsApplications;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "protocolApplication")
	public RunRobot getRunRobot() {
		return this.runRobot;
	}

	public void setRunRobot(RunRobot runRobot) {
		this.runRobot = runRobot;
	}

}
