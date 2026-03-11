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
package fr.edyp.epims.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class LogFileAppender {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogFileAppender.class);

  public void updateInstrumentLogFile(String dataToAppend, File f) throws IOException{
    if(!f.exists()){
      LOGGER.warn("File {} does not exist", f.getAbsolutePath());
      boolean result = f.createNewFile();
      if(!result) {
        LOGGER.error("Error creating file {}", f.getAbsolutePath());
        throw new IOException("Error creating file "+f.getAbsolutePath());
      }
    }
    FileUtils.append(f, dataToAppend);
  }
}
