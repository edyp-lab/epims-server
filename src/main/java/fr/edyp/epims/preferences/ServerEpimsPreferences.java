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

package fr.edyp.epims.preferences;

import java.io.File;

public class ServerEpimsPreferences {

    private static FilePreferences m_preferences;

    public static java.util.prefs.Preferences root() {

        if (m_preferences == null) {
            initPreferences(null);
        }

        return m_preferences;
    }

    public static void initPreferences(String path) {

        if (path == null) {
            path = getUserHome()+ File.separator+"EpimsServerPreferences.properties";  // by default Preferences.properties is saved in the local directory of the application
        } else {
            path = path+File.separator+"EpimsServerPreferences.properties";
        }

        //System.out.println(new File(path).getAbsolutePath());

        m_preferences = new FilePreferences(new File(path), null, "");
    }


    private static String getUserHome() {
        return "./";
    }

}
