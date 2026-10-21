package fr.edyp.epims.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class MiscUtils {

  public static Date asDate(LocalDate localDate) {
    return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
  }


}
