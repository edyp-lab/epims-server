/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.controller;


import fr.edyp.epims.json.InstrumentLogInfoJson;
import fr.edyp.epims.path.PathManager;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import fr.edyp.epims.util.LogFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

@RestController
@RequestMapping("/api")
public class LogFileController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogFileController.class);

  @Autowired
  LogFileAppender fileAppender;

  @Autowired
  private Environment env;

  @PostMapping("/updateServerLogFile")
  public ResponseEntity<String> updateServerFile(@RequestBody InstrumentLogInfoJson instrumentLogInfo) {
    String instrumentName = instrumentLogInfo.getInstrumentName();
    String logData = instrumentLogInfo.getLogData();
    if( instrumentName == null || logData == null || instrumentName.isEmpty() || logData.isEmpty())
      return new ResponseEntity<>("Bad request", HttpStatus.BAD_REQUEST);

    try {
      File instrumentLogFile = getInstrLogFile(instrumentName);
      fileAppender.updateInstrumentLogFile(logData, instrumentLogFile);
      return new ResponseEntity<String>("OK", HttpStatus.OK);
    } catch (Exception e) {
      LOGGER.error("Error updating log file for {}", instrumentName, e);
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }


  private File getInstrLogFile(String instrumentName ) throws BackingStoreException, IOException {
    Preferences preferences = ServerEpimsPreferences.root();
    String epimsRoot = preferences.get(PreferencesKeys.PIMS_ROOT, env.getProperty("epims.repository"));
    String systemPath = PathManager.getSystemRelativePath();
    File baseFile =new File(epimsRoot+ "/"+systemPath);
    File targetFile =new File(baseFile, instrumentName+".log").getCanonicalFile();
    if (!targetFile.getPath().startsWith(baseFile.getCanonicalPath())) {
      throw new SecurityException("Invalid file path");
    }
    return targetFile;
  }


}
