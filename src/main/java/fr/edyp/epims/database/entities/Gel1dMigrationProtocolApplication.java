package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Gel1dMigrationProtocolApplication generated by hbm2java
 */
@Entity
@Table(name = "gel_1d_migration_protocol_application", schema = "public")
public class Gel1dMigrationProtocolApplication implements java.io.Serializable {

	private Gel1dMigrationProtocolApplicationId id;

	public Gel1dMigrationProtocolApplication() {
	}

	public Gel1dMigrationProtocolApplication(Gel1dMigrationProtocolApplicationId id) {
		this.id = id;
	}

	@EmbeddedId

	@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "id")),
			@AttributeOverride(name = "name", column = @Column(name = "name", length = 50)),
			@AttributeOverride(name = "date", column = @Column(name = "date")),
			@AttributeOverride(name = "actor", column = @Column(name = "actor")),
			@AttributeOverride(name = "comment", column = @Column(name = "comment")),
			@AttributeOverride(name = "protocol", column = @Column(name = "protocol")),
			@AttributeOverride(name = "result", column = @Column(name = "result")),
			@AttributeOverride(name = "massStart", column = @Column(name = "mass_start")),
			@AttributeOverride(name = "massEnd", column = @Column(name = "mass_end")),
			@AttributeOverride(name = "denaturingAgent", column = @Column(name = "denaturing_agent")) })
	public Gel1dMigrationProtocolApplicationId getId() {
		return this.id;
	}

	public void setId(Gel1dMigrationProtocolApplicationId id) {
		this.id = id;
	}

}
