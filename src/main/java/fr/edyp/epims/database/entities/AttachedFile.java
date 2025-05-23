package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final


import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * AttachedFile generated by hbm2java
 */
@Entity
@Table(name = "attached_file", schema = "public")
public class AttachedFile implements java.io.Serializable {

	private int id;
	private Date date;
	private String path;
	private String mimetype;
	private String name;
	private Boolean archived;
	private Double sizeMo;
	private Set<FileLink> fileLinks = new HashSet<FileLink>(0);
	private Set<FileTags> fileTagses = new HashSet<FileTags>(0);

	public AttachedFile() {
	}

	public AttachedFile(int id, Date date) {
		this.id = id;
		this.date = date;
	}

	public AttachedFile(int id, Date date, String path, String mimetype, String name, Boolean archived,
			Double sizeMo, Set<FileLink> fileLinks, Set<FileTags> fileTagses) {
		this.id = id;
		this.date = date;
		this.path = path;
		this.mimetype = mimetype;
		this.name = name;
		this.archived = archived;
		this.sizeMo = sizeMo;
		this.fileLinks = fileLinks;
		this.fileTagses = fileTagses;
	}

	@Id
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "attached_file_generator")
	@SequenceGenerator(name = "attached_file_generator", sequenceName = "attached_file_id_seq", allocationSize = 1)
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "date", nullable = false)
	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Column(name = "path", length = 100)
	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Column(name = "mimetype", length = 100)
	public String getMimetype() {
		return this.mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	@Column(name = "name", length = 60)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "archived")
	public Boolean getArchived() {
		return this.archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	@Column(name = "size_mo", precision = 17, scale = 17)
	public Double getSizeMo() {
		return this.sizeMo;
	}

	public void setSizeMo(Double sizeMo) {
		this.sizeMo = sizeMo;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "attachedFile")
	public Set<FileLink> getFileLinks() {
		return this.fileLinks;
	}

	public void setFileLinks(Set<FileLink> fileLinks) {
		this.fileLinks = fileLinks;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "attachedFile")
	public Set<FileTags> getFileTagses() {
		return this.fileTagses;
	}

	public void setFileTagses(Set<FileTags> fileTagses) {
		this.fileTagses = fileTagses;
	}

}
