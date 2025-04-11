package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * MsmsProtocol generated by hbm2java
 */
@Entity
@Table(name = "msms_protocol", schema = "public")
public class MsmsProtocol implements java.io.Serializable {

	private int id;
	private String selectedMass;
	private String runTime;

	public MsmsProtocol() {
	}

	public MsmsProtocol(int id) {
		this.id = id;
	}

	public MsmsProtocol(int id, String selectedMass, String runTime) {
		this.id = id;
		this.selectedMass = selectedMass;
		this.runTime = runTime;
	}

	@Id

	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "selected_mass")
	public String getSelectedMass() {
		return this.selectedMass;
	}

	public void setSelectedMass(String selectedMass) {
		this.selectedMass = selectedMass;
	}

	@Column(name = "run_time")
	public String getRunTime() {
		return this.runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}

}
