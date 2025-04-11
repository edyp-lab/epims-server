package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * PreparationProtocolApplicationId generated by hbm2java
 */
@Embeddable
public class PreparationProtocolApplicationId implements java.io.Serializable {

	private Integer id;
	private String name;
	private String date;
	private String actor;
	private String comment;
	private Integer protocol;

	public PreparationProtocolApplicationId() {
	}

	public PreparationProtocolApplicationId(Integer id, String name, String date, String actor,
			String comment, Integer protocol) {
		this.id = id;
		this.name = name;
		this.date = date;
		this.actor = actor;
		this.comment = comment;
		this.protocol = protocol;
	}

	@Column(name = "id")
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "name", length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "date")
	public String getDate() {
		return this.date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Column(name = "actor")
	public String getActor() {
		return this.actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	@Column(name = "comment")
	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Column(name = "protocol")
	public Integer getProtocol() {
		return this.protocol;
	}

	public void setProtocol(Integer protocol) {
		this.protocol = protocol;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof PreparationProtocolApplicationId))
			return false;
		PreparationProtocolApplicationId castOther = (PreparationProtocolApplicationId) other;

		return ((this.getId() == castOther.getId())
				|| (this.getId() != null && castOther.getId() != null && this.getId().equals(castOther.getId())))
				&& ((this.getName() == castOther.getName()) || (this.getName() != null && castOther.getName() != null
						&& this.getName().equals(castOther.getName())))
				&& ((this.getDate() == castOther.getDate()) || (this.getDate() != null && castOther.getDate() != null
						&& this.getDate().equals(castOther.getDate())))
				&& ((this.getActor() == castOther.getActor()) || (this.getActor() != null
						&& castOther.getActor() != null && this.getActor().equals(castOther.getActor())))
				&& ((this.getComment() == castOther.getComment()) || (this.getComment() != null
						&& castOther.getComment() != null && this.getComment().equals(castOther.getComment())))
				&& ((this.getProtocol() == castOther.getProtocol()) || (this.getProtocol() != null
						&& castOther.getProtocol() != null && this.getProtocol().equals(castOther.getProtocol())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getId() == null ? 0 : this.getId().hashCode());
		result = 37 * result + (getName() == null ? 0 : this.getName().hashCode());
		result = 37 * result + (getDate() == null ? 0 : this.getDate().hashCode());
		result = 37 * result + (getActor() == null ? 0 : this.getActor().hashCode());
		result = 37 * result + (getComment() == null ? 0 : this.getComment().hashCode());
		result = 37 * result + (getProtocol() == null ? 0 : this.getProtocol().hashCode());
		return result;
	}

}
