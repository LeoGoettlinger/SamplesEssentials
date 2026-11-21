package de.oliver.fancyanalytics.sdk.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtils {
   public static String getAsString(Throwable throwable) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      throwable.printStackTrace(printWriter);
      printWriter.close();
      return stringWriter.toString();
   }
}
