package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * VirtualPlate generated by hbm2java
 */
@Entity
@Table(name = "virtual_plate", schema = "public")
public class VirtualPlate implements java.io.Serializable {

	private String name;
	private Actor actor;
	private Date plannedDate;
	private Boolean locked;
	private Integer XSize;
	private Integer YSize;
	private Set<VirtualWell> virtualWells = new HashSet<>(0);

	public VirtualPlate() {
	}

	public VirtualPlate(String name) {
		this.name = name;
	}

	public VirtualPlate(String name, Actor actor, Date plannedDate, Boolean locked, Integer XSize,
			Integer YSize, Set<VirtualWell> virtualWells) {
		this.name = name;
		this.actor = actor;
		this.plannedDate = plannedDate;
		this.locked = locked;
		this.XSize = XSize;
		this.YSize = YSize;
		this.virtualWells = virtualWells;
	}

	@Id

	@Column(name = "name", unique = true, nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "locker")
	public Actor getActor() {
		return this.actor;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	@Column(name = "planned_date")
	public Date getPlannedDate() {
		return this.plannedDate;
	}

	public void setPlannedDate(Date plannedDate) {
		this.plannedDate = plannedDate;
	}

	@Column(name = "locked")
	public Boolean getLocked() {
		return this.locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	@Column(name = "x_size")
	public Integer getXSize() {
		return this.XSize;
	}

	public void setXSize(Integer XSize) {
		this.XSize = XSize;
	}

	@Column(name = "y_size")
	public Integer getYSize() {
		return this.YSize;
	}

	public void setYSize(Integer YSize) {
		this.YSize = YSize;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "virtualPlate")
	public Set<VirtualWell> getVirtualWells() {
		return this.virtualWells;
	}

	public void setVirtualWells(Set<VirtualWell> virtualWells) {
		this.virtualWells = virtualWells;
	}

}
