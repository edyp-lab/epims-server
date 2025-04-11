package fr.edyp.epims.database.entities;
// Generated 10 mai 2021 � 11:47:01 by Hibernate Tools 5.2.12.Final

import javax.persistence.*;

/**
 * ProgContacts generated by hbm2java
 */
@Entity
@Table(name = "prog_contacts", schema = "public")
public class ProgContacts implements java.io.Serializable {

	private int id;
	private Contact contact;
	private Program program;

	public ProgContacts() {
	}

	public ProgContacts(Contact contact, Program program) {
		this.contact = contact;
		this.program = program;
	}

	public ProgContacts(int id, Contact contact, Program program) {
		this.id = id;
		this.contact = contact;
		this.program = program;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "prog_contacts_generator")
	@SequenceGenerator(name = "prog_contacts_generator", sequenceName = "prog_contacts_id_seq", allocationSize = 1)
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contact", nullable = false)
	public Contact getContact() {
		return this.contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "program", nullable = false)
	public Program getProgram() {
		return this.program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}

}
