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

package fr.edyp.epims;

import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

@Component
public class StartupHookListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupHookListener.class);

    @Autowired
    private Environment env;

    private boolean m_firstTime = true;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (m_firstTime) {
            m_firstTime = false;

            // needed initialization of properties
            Preferences preferences = ServerEpimsPreferences.root();
            boolean needFlush = false;
            try {
                if (preferences.get(PreferencesKeys.FTP_HOST, null) == null) {
                    // Parameters are not in the preference file
                    LOGGER.error(" NO FTP parameters in Preference file !! SET general value... Should be changed");
                    preferences.put(PreferencesKeys.FTP_HOST, "epims-host");
                    preferences.put(PreferencesKeys.FTP_LOG, "epims");
                    preferences.put(PreferencesKeys.FTP_PASSWORD, "epims-pwd");
                    preferences.put(PreferencesKeys.FTP_HOME, env.getProperty("epims.ftp.home"));
                    preferences.put(PreferencesKeys.REPOSITORY_DEFAULT_ABC_LETTER, "a");
                    needFlush = true;
                }

                if (preferences.get(PreferencesKeys.PIMS_SYSTEM_RELATIVE_PATH, null) == null) {
                    // Parameters are not in the preference file
                    preferences.put(PreferencesKeys.PIMS_SYSTEM_RELATIVE_PATH, "system");
                    needFlush = true;
                }

                if (preferences.get(PreferencesKeys.PIMS_ROOT, null) == null) {
                    // Parameters are not in the preference file
                    preferences.put(PreferencesKeys.PIMS_ROOT, env.getProperty("epims.repository"));
                    needFlush = true;
                }

                if (preferences.get(PreferencesKeys.JMS_HOST, null) == null) {
                    // Parameters are not in the preference file
                    preferences.put(PreferencesKeys.JMS_HOST, env.getProperty("spring.activemq.broker-url"));
                    needFlush = true;
                }
                if (needFlush) {
                    preferences.flush();
                }
            } catch (Exception e) {
                // should not happen
            }
        }
    }
}