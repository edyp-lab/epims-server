package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * FileTags generated by hbm2java
 */
@Entity
@Table(name = "file_tags", schema = "public")
public class FileTags implements java.io.Serializable {

	private FileTagsId id;
	private AttachedFile attachedFile;
	private Tag tag;
	private int index;

	public FileTags() {
	}


	public FileTags(FileTagsId id, AttachedFile attachedFile, Tag tag, int index) {
		this.id = id;
		this.attachedFile = attachedFile;
		this.tag = tag;
		this.index = index;
	}

	@EmbeddedId

	@AttributeOverrides({ @AttributeOverride(name = "file", column = @Column(name = "file", nullable = false)),
			@AttributeOverride(name = "tag", column = @Column(name = "tag", nullable = false)) })
	public FileTagsId getId() {
		return this.id;
	}

	public void setId(FileTagsId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file", nullable = false, insertable = false, updatable = false)
	public AttachedFile getAttachedFile() {
		return this.attachedFile;
	}

	public void setAttachedFile(AttachedFile attachedFile) {
		this.attachedFile = attachedFile;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag", nullable = false, insertable = false, updatable = false)
	public Tag getTag() {
		return this.tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	@Column(name = "index", nullable = false)
	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
