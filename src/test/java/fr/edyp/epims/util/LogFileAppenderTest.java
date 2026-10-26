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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class LogFileAppenderTest {

  static LogFileAppender logAppender;

  @BeforeAll
  static void setUp() throws Exception {
    logAppender  = new LogFileAppender();
  }

  @Test
  void updateInstrumentLogFile() {
    File f = new File(".\\test.txt");
    f.delete();
    String data = "2025-12-05 15:34:44 Test Data djhkkl jjjj ";
    String content ="";
    try {
      logAppender.updateInstrumentLogFile(data, f);
      content = org.apache.commons.io.FileUtils.readFileToString(f, "UTF-8");
    } catch (IOException e) {
      fail("Error updating log file", e);
    }
    assertTrue(f.exists());
    assertTrue(content.contains(data));

    String data2 = "2025-12-09 14:44:41 New Data to add  ";
    try {
      logAppender.updateInstrumentLogFile(data2, f);
      content = org.apache.commons.io.FileUtils.readFileToString(f, "UTF-8");
    } catch (IOException e) {
      fail("Error updating log file", e);
    }
    assertTrue(f.exists());
    assertTrue(content.contains(data));
    assertTrue(content.contains(data2));
  }
}