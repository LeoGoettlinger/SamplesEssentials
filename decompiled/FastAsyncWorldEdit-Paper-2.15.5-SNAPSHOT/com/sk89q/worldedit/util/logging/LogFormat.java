package com.sk89q.worldedit.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormat extends Formatter {
   public static final String DEFAULT_FORMAT = "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s]: %5$s%6$s%n";
   private final Date dat = new Date();
   private final String format;

   public LogFormat() {
      this(null);
   }

   public LogFormat(String format) {
      if (format == null || format.isEmpty()) {
         format = "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s]: %5$s%6$s%n";
      }

      try {
         String.format(format, new Date(), "", "", "", "", "");
      } catch (IllegalArgumentException var3) {
         format = "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s]: %5$s%6$s%n";
      }

      this.format = format;
   }

   @Override
   public String format(LogRecord record) {
      this.dat.setTime(record.getMillis());
      String source;
      if (record.getSourceClassName() != null) {
         source = record.getSourceClassName();
         if (record.getSourceMethodName() != null) {
            source = source + " " + record.getSourceMethodName();
         }
      } else {
         source = record.getLoggerName();
      }

      String message = this.formatMessage(record);
      String throwable = "";
      if (record.getThrown() != null) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         pw.println();
         record.getThrown().printStackTrace(pw);
         pw.close();
         throwable = sw.toString();
      }

      return String.format(this.format, this.dat, source, record.getLoggerName(), record.getLevel().getName(), message, throwable);
   }
}
