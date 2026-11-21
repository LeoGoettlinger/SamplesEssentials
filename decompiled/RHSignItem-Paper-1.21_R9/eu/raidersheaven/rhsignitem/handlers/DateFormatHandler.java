package eu.raidersheaven.rhsignitem.handlers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatHandler {
   public static String getCurrentDate(String pattern) {
      return new SimpleDateFormat(pattern).format(new Date());
   }
}
