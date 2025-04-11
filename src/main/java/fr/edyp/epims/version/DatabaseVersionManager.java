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

package fr.edyp.epims.version;

import fr.edyp.epims.json.DatabaseVersionJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatabaseVersionManager {


    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseVersionManager.class);

    private static final int SERVER_VERSION = 1; // Client and server version must be the same

    private static DatabaseVersionManager m_singleton = null;

    private DatabaseVersionJson m_databaseVersion = new DatabaseVersionJson();

    private DatabaseVersionManager() {
        m_databaseVersion.setServerVersion(SERVER_VERSION);
    }

    public static DatabaseVersionManager getSingleton() {

        if (m_singleton == null) {
            m_singleton = new DatabaseVersionManager();
        }

        return m_singleton;
    }

    public DatabaseVersionJson getVersion() {
        return m_databaseVersion;
    }

    public Integer bumpVersion(Class c, String login) {

        LOGGER.info("bumpVersion "+c.getName()+" "+(login != null ? login : ""));

        return m_databaseVersion.bumpVersion(c, login);
    }

    public Integer getVersion(Class c) {

        Integer version = m_databaseVersion.getVersion(c);

        LOGGER.info("getVersion "+c.getName()+" "+version);

        return version;
    }

    public String getLogin(Class c) {
        return m_databaseVersion.getLogin (c);
    }
}
