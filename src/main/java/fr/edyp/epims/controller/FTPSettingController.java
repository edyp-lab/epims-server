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

package fr.edyp.epims.controller;

import fr.edyp.epims.json.FtpConfigurationJson;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.prefs.Preferences;

@RestController
@RequestMapping("/api")
public class FTPSettingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FTPSettingController.class);

    @Autowired
    private Environment env;

    @GetMapping("/ftpsettings")
    public ResponseEntity<FtpConfigurationJson> ftpsettings() {
        try {

            //
            Preferences preferences = ServerEpimsPreferences.root();
            if (preferences.get(PreferencesKeys.FTP_HOST, null) == null) {
                // Parameters are not in the preference file
                LOGGER.error(" NO FTP parameters in Preference file !! SET general value... Should be changed");
                preferences.put(PreferencesKeys.FTP_HOST, "epims-host");
                preferences.put(PreferencesKeys.FTP_LOG, "epims");
                preferences.put(PreferencesKeys.FTP_PASSWORD, "epims-pwd");
                preferences.put(PreferencesKeys.FTP_HOME, env.getProperty("epims.ftp.home"));
                preferences.put(PreferencesKeys.REPOSITORY_DEFAULT_ABC_LETTER, "a");
                preferences.flush();
            }

            String ftpHost = preferences.get(PreferencesKeys.FTP_HOST, "epims-host");
            String ftpLog = preferences.get(PreferencesKeys.FTP_LOG, "epims");
            String ftpPassword = preferences.get(PreferencesKeys.FTP_PASSWORD, "epims-pwd");
            String ftpHome = preferences.get(PreferencesKeys.FTP_HOME, env.getProperty("epims.ftp.home"));


            FtpConfigurationJson configuration = new FtpConfigurationJson(ftpHost, ftpLog, ftpPassword, ftpHome, null);

            return new ResponseEntity<>(configuration, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /api/ftpsettings", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
