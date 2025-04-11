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

import fr.edyp.epims.json.AcquisitionFileMessageJson;
import fr.edyp.epims.path.PathManager;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.prefs.Preferences;

@RestController
@RequestMapping("/api")
public class PathController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathController.class);

    @Autowired
    PathManager pathManager;

    @GetMapping("/pimsSystemRelativePath")
    public ResponseEntity<String> pimsSystemRelativePath() {
        try {

            //
            Preferences preferences = ServerEpimsPreferences.root();
            if (preferences.get(PreferencesKeys.PIMS_SYSTEM_RELATIVE_PATH, null) == null) {
                // Parameters are not in the preference file
                preferences.put(PreferencesKeys.PIMS_SYSTEM_RELATIVE_PATH, "system");
                preferences.flush();
            }

            String path = preferences.get(PreferencesKeys.PIMS_SYSTEM_RELATIVE_PATH, "system");

            return new ResponseEntity<>(path, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /api/pimsSystemRelativePath", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/spectraRelativePath")
    public ResponseEntity<String> spectraRelativePath() {
        try {

            //
            Preferences preferences = ServerEpimsPreferences.root();
            if (preferences.get(PreferencesKeys.SPECTRA_RELATIVE_PATH, null) == null) {
                // Parameters are not in the preference file
                preferences.put(PreferencesKeys.SPECTRA_RELATIVE_PATH, "samples/data/SPECTRA");
                preferences.flush();
            }

            String path = preferences.get(PreferencesKeys.SPECTRA_RELATIVE_PATH, "samples/data/SPECTRA");

            return new ResponseEntity<>(path, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /api/spectraRelativePath", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/acquisitiondestpath")
    public ResponseEntity<String> acquisitionDestPath(@RequestBody AcquisitionFileMessageJson acquisitionFileMessageJson) {

        try {
            String path = pathManager.getAcquisitionDestinationPath(acquisitionFileMessageJson);

            return new ResponseEntity<>(path, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /api/acquisitiondestpath", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
