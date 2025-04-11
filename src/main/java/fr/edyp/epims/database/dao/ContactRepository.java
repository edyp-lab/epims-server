/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */

package fr.edyp.epims.database.dao;

import fr.edyp.epims.database.entities.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ContactRepository extends JpaRepository<Contact, Integer> {
    List<Contact> findByLastName(String lastName);

}



/*
public interface IContactDAO {

    public void save(Contact persistentInstance);

    public void delete(Contact persistentInstance);

    public void update(Contact persistentInstance);

    public void refresh(Contact persistentInstance);

    public void attachDirty(Contact instance);

    public void attachClean(Contact instance);

    public Contact merge(Contact detachedInstance);

    public Contact findById(java.lang.Integer id);

    public List<Contact> findByExample(Contact instance);

    public List<Contact> findByProperty(String propertyName, Object value);

    public List<Contact> findAll();

    public List<Contact> find(String query, Object[] values);

}*/
