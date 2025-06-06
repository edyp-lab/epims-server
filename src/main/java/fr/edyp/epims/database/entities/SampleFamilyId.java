package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * SampleFamilyId generated by hbm2java
 */
@Embeddable
public class SampleFamilyId implements java.io.Serializable {

	private String father;
	private String son;

	public SampleFamilyId() {
	}

	public SampleFamilyId(String father, String son) {
		this.father = father;
		this.son = son;
	}

	@Column(name = "father", nullable = false)
	public String getFather() {
		return this.father;
	}

	public void setFather(String father) {
		this.father = father;
	}

	@Column(name = "son", nullable = false)
	public String getSon() {
		return this.son;
	}

	public void setSon(String son) {
		this.son = son;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SampleFamilyId))
			return false;
		SampleFamilyId castOther = (SampleFamilyId) other;

		return ((this.getFather() == castOther.getFather()) || (this.getFather() != null
				&& castOther.getFather() != null && this.getFather().equals(castOther.getFather())))
				&& ((this.getSon() == castOther.getSon()) || (this.getSon() != null && castOther.getSon() != null
						&& this.getSon().equals(castOther.getSon())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getFather() == null ? 0 : this.getFather()).hashCode();
		result = 37 * result + (getSon() == null ? 0 : this.getSon()).hashCode();
		return result;
	}

}
