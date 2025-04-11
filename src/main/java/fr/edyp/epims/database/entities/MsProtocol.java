package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * MsProtocol generated by hbm2java
 */
@Entity
@Table(name = "ms_protocol", schema = "public")
public class MsProtocol implements java.io.Serializable {

	private int id;
	private String laserWavelength;
	private String laserPower;
	private String msType;

	public MsProtocol() {
	}

	public MsProtocol(int id, String msType) {
		this.id = id;
		this.msType = msType;
	}

	public MsProtocol(int id, String laserWavelength, String laserPower, String msType) {
		this.id = id;
		this.laserWavelength = laserWavelength;
		this.laserPower = laserPower;
		this.msType = msType;
	}

	@Id

	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "laser_wavelength")
	public String getLaserWavelength() {
		return this.laserWavelength;
	}

	public void setLaserWavelength(String laserWavelength) {
		this.laserWavelength = laserWavelength;
	}

	@Column(name = "laser_power")
	public String getLaserPower() {
		return this.laserPower;
	}

	public void setLaserPower(String laserPower) {
		this.laserPower = laserPower;
	}

	@Column(name = "ms_type", nullable = false)
	public String getMsType() {
		return this.msType;
	}

	public void setMsType(String msType) {
		this.msType = msType;
	}

}
