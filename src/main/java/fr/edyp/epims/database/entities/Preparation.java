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
 * Preparation generated by hbm2java
 */
@Entity
@Table(name = "preparation", schema = "public")
public class Preparation implements java.io.Serializable {

	private int id;
	private ProtocolApplication protocolApplication;

	public Preparation() {
	}

	public Preparation(ProtocolApplication protocolApplication) {
		this.protocolApplication = protocolApplication;
	}

	@GenericGenerator(name = "org.o7planning.generateentities.PreparationIdGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "protocolApplication"))
	@Id
	@GeneratedValue(generator = "org.o7planning.generateentities.PreparationIdGenerator")

	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	public ProtocolApplication getProtocolApplication() {
		return this.protocolApplication;
	}

	public void setProtocolApplication(ProtocolApplication protocolApplication) {
		this.protocolApplication = protocolApplication;
	}

}
