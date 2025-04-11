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

import fr.edyp.epims.archivage.ArchivageAsyncService;
import fr.edyp.epims.archivage.ArchivingData;
import fr.edyp.epims.database.dao.ArchiveService;
import fr.edyp.epims.database.dao.AttachedFileRepository;
import fr.edyp.epims.database.dao.StudyRepository;
import fr.edyp.epims.json.ArchivingInfoJson;
import fr.edyp.epims.json.ControlAcquisitionArchivableJson;
import fr.edyp.epims.path.PathManager;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;


@RestController
@RequestMapping("/api")
public class ArchiveController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveController.class);

    @Autowired
    ArchiveService archiveService;

    @Autowired
    ArchivageAsyncService archivageAsyncService;

    @Autowired
    ArchivingData archivingData;


    @GetMapping("/archivepath")
    public ResponseEntity<String> archivepath() {

        Preferences preferences = ServerEpimsPreferences.root();
        String archiveRoot = preferences.get(PreferencesKeys.ARCHIVE_ROOT, "");

        return new ResponseEntity<>(archiveRoot, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/controltoarchivelist")
    public ResponseEntity<List<ControlAcquisitionArchivableJson>> controltoarchive() {

        // read file link to fix
        List<ControlAcquisitionArchivableJson> controlAcquisitionArchivableJsonList = archiveService.controlToBeArchivedByMonthYearInstrument();

        return new ResponseEntity<>(controlAcquisitionArchivableJsonList, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/archive")
    public ResponseEntity<String> archive(@RequestBody LinkedList<ArchivingInfoJson> archivingInfoJsonList) {

        // ask for archivage (asynchronous : returns immediately)
        archivageAsyncService.executeTask(archivingInfoJsonList);

        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/archivepoll")
    public ResponseEntity<ArchivingInfoJson[]> archivepoll() {

        // ask for archivage (asynchronous : returns immediately)
        ArchivingInfoJson[] archivingInfoJsonArray = archivingData.getAllArchiving();

        return new ResponseEntity<>(archivingInfoJsonArray, HttpStatus.OK);
    }


}
