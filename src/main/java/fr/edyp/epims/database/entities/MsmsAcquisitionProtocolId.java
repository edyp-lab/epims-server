package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * MsmsAcquisitionProtocolId generated by hbm2java
 */
@Embeddable
public class MsmsAcquisitionProtocolId implements java.io.Serializable {

	private String title;
	private Integer reference;
	private Integer version;
	private String creationDate;
	private String softwareVersion;
	private String softwareName;
	private String parametersFile;
	private String selectedMass;
	private String runTime;

	public MsmsAcquisitionProtocolId() {
	}

	public MsmsAcquisitionProtocolId(String title, Integer reference, Integer version, String creationDate,
			String softwareVersion, String softwareName, String parametersFile,
			String selectedMass, String runTime) {
		this.title = title;
		this.reference = reference;
		this.version = version;
		this.creationDate = creationDate;
		this.softwareVersion = softwareVersion;
		this.softwareName = softwareName;
		this.parametersFile = parametersFile;
		this.selectedMass = selectedMass;
		this.runTime = runTime;
	}

	@Column(name = "title")
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "reference")
	public Integer getReference() {
		return this.reference;
	}

	public void setReference(Integer reference) {
		this.reference = reference;
	}

	@Column(name = "version")
	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "creation_date")
	public String getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	@Column(name = "software_version")
	public String getSoftwareVersion() {
		return this.softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	@Column(name = "software_name")
	public String getSoftwareName() {
		return this.softwareName;
	}

	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}

	@Column(name = "parameters_file")
	public String getParametersFile() {
		return this.parametersFile;
	}

	public void setParametersFile(String parametersFile) {
		this.parametersFile = parametersFile;
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

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MsmsAcquisitionProtocolId))
			return false;
		MsmsAcquisitionProtocolId castOther = (MsmsAcquisitionProtocolId) other;

		return ((this.getTitle() == castOther.getTitle()) || (this.getTitle() != null && castOther.getTitle() != null
				&& this.getTitle().equals(castOther.getTitle())))
				&& ((this.getReference() == castOther.getReference()) || (this.getReference() != null
						&& castOther.getReference() != null && this.getReference().equals(castOther.getReference())))
				&& ((this.getVersion() == castOther.getVersion()) || (this.getVersion() != null
						&& castOther.getVersion() != null && this.getVersion().equals(castOther.getVersion())))
				&& ((this.getCreationDate() == castOther.getCreationDate())
						|| (this.getCreationDate() != null && castOther.getCreationDate() != null
								&& this.getCreationDate().equals(castOther.getCreationDate())))
				&& ((this.getSoftwareVersion() == castOther.getSoftwareVersion())
						|| (this.getSoftwareVersion() != null && castOther.getSoftwareVersion() != null
								&& this.getSoftwareVersion().equals(castOther.getSoftwareVersion())))
				&& ((this.getSoftwareName() == castOther.getSoftwareName())
						|| (this.getSoftwareName() != null && castOther.getSoftwareName() != null
								&& this.getSoftwareName().equals(castOther.getSoftwareName())))
				&& ((this.getParametersFile() == castOther.getParametersFile())
						|| (this.getParametersFile() != null && castOther.getParametersFile() != null
								&& this.getParametersFile().equals(castOther.getParametersFile())))
				&& ((this.getSelectedMass() == castOther.getSelectedMass())
						|| (this.getSelectedMass() != null && castOther.getSelectedMass() != null
								&& this.getSelectedMass().equals(castOther.getSelectedMass())))
				&& ((this.getRunTime() == castOther.getRunTime()) || (this.getRunTime() != null
						&& castOther.getRunTime() != null && this.getRunTime().equals(castOther.getRunTime())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getTitle() == null ? 0 : this.getTitle().hashCode());
		result = 37 * result + (getReference() == null ? 0 : this.getReference().hashCode());
		result = 37 * result + (getVersion() == null ? 0 : this.getVersion().hashCode());
		result = 37 * result + (getCreationDate() == null ? 0 : this.getCreationDate().hashCode());
		result = 37 * result + (getSoftwareVersion() == null ? 0 : this.getSoftwareVersion().hashCode());
		result = 37 * result + (getSoftwareName() == null ? 0 : this.getSoftwareName().hashCode());
		result = 37 * result + (getParametersFile() == null ? 0 : this.getParametersFile().hashCode());
		result = 37 * result + (getSelectedMass() == null ? 0 : this.getSelectedMass().hashCode());
		result = 37 * result + (getRunTime() == null ? 0 : this.getRunTime().hashCode());
		return result;
	}

}
