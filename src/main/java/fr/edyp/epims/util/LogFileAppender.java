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
