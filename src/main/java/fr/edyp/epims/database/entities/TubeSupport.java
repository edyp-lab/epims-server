package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * TubeSupport generated by hbm2java
 */
@Entity
@Table(name = "tube_support", schema = "public")
public class TubeSupport implements java.io.Serializable {

	private TubeSupportId id;

	public TubeSupport() {
	}

	public TubeSupport(TubeSupportId id) {
		this.id = id;
	}

	@EmbeddedId

	@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "id")),
			@AttributeOverride(name = "sample", column = @Column(name = "sample")),
			@AttributeOverride(name = "supportSeqRank", column = @Column(name = "support_seq_rank")) })
	public TubeSupportId getId() {
		return this.id;
	}

	public void setId(TubeSupportId id) {
		this.id = id;
	}

}
