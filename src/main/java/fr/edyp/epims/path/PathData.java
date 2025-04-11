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

package fr.edyp.epims.path;

import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.prefs.Preferences;

@Component
public class PathData {

    public final static String UNDEF_DIR_NAME="_UNCLASS_";
    public final static String PIMS_REPOSITORY_1="a";
    public final static String PIMS_REPOSITORY_2="b";
    public final static String PIMS_REPOSITORY_3="c";
    public final static String PIMS_REPOSITORY_4="d";
    public final static String PIMS_REPOSITORY_5="e";
    public final static String PIMS_SHARE="a/share";
    public final static String PIMS_SYSTEM="adm";
    public final static String PIMS_ARCHIVE="adm/archive";
    public final static String PIMS_ARCHIVE_FILE="studyArchived.txt";
    public final static String PIMS_DROP_ZONE="adm/xfer";

    private HashMap<String, String> m_map = null;

    @Autowired
    private Environment env;

    private void init() {

        if (m_map != null) {
            return;
        }

        Preferences preferences = ServerEpimsPreferences.root();
        final String PIMS_ROOT = preferences.get(PreferencesKeys.PIMS_ROOT, env.getProperty("epims.repository"));

        m_map = new HashMap();
        m_map.put(ResourceManager.UNDEFINED_DIR, UNDEF_DIR_NAME);
        m_map.put(ResourceManager.PIMS_ROOT, PIMS_ROOT);
        m_map.put(ResourceManager.PIMS_REPOSITORY_PREFIX+"1", PIMS_REPOSITORY_1);
        m_map.put(ResourceManager.PIMS_REPOSITORY_PREFIX+"2", PIMS_REPOSITORY_2);
        m_map.put(ResourceManager.PIMS_REPOSITORY_PREFIX+"3", PIMS_REPOSITORY_3);
        m_map.put(ResourceManager.PIMS_REPOSITORY_PREFIX+"4", PIMS_REPOSITORY_4);
        m_map.put(ResourceManager.PIMS_REPOSITORY_PREFIX+"5", PIMS_REPOSITORY_5);
        m_map.put(ResourceManager.PIMS_SHARE, PIMS_SHARE);
        m_map.put(ResourceManager.PIMS_SYSTEM, PIMS_SYSTEM);
        m_map.put(ResourceManager.PIMS_ARCHIVE, PIMS_ARCHIVE);
        m_map.put(ResourceManager.PIMS_ARCHIVE_FILE, PIMS_ARCHIVE_FILE);
        m_map.put(ResourceManager.PIMS_DROP_ZONE, PIMS_DROP_ZONE);
    }

    public String getPath(String pathKey) {
        init();
        return m_map.get(pathKey);
    }


}
