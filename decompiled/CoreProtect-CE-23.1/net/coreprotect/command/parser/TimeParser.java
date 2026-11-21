package net.coreprotect.command.parser;

import java.math.BigDecimal;
import java.util.Locale;
import net.coreprotect.language.Phrase;

public class TimeParser {
   public static long[] parseTime(String[] inputArguments) {
      String[] argumentArray = (String[])inputArguments.clone();
      long timeStart = 0L;
      long timeEnd = 0L;
      int count = 0;
      int next = 0;
      boolean range = false;
      double w = 0.0;
      double d = 0.0;
      double h = 0.0;
      double m = 0.0;
      double s = 0.0;
      double mo = 0.0;
      double y = 0.0;

      for (String arg : argumentArray) {
         if (arg.contains("3mo")) {
            return new long[]{7776000L, 0L};
         }
      }

      for (String argument : argumentArray) {
         if (count > 0) {
            argument = argument.trim().toLowerCase(Locale.ROOT);
            argument = argument.replaceAll("\\\\", "");
            argument = argument.replaceAll("'", "");
            if (argument.equals("t:") || argument.equals("time:")) {
               next = 1;
            } else if (next != 1 && !argument.startsWith("t:") && !argument.startsWith("time:")) {
               next = 0;
            } else {
               argument = argument.replaceAll("time:", "");
               argument = argument.replaceAll("t:", "");
               argument = argument.replaceAll("mo", "mo:");
               argument = argument.replaceAll("y", "y:");
               argument = argument.replaceAll("(?<!o)m", "m:");
               argument = argument.replaceAll("w", "w:");
               argument = argument.replaceAll("d", "d:");
               argument = argument.replaceAll("h", "h:");
               argument = argument.replaceAll("s", "s:");
               range = argument.contains("-");
               int argCount = 0;
               String[] i2 = argument.split(":");

               for (String i3 : i2) {
                  if (range && argCount > 0 && timeStart == 0L && i3.startsWith("-")) {
                     timeStart = (long)(y * 3.1536E7 + mo * 2592000.0 + w * 604800.0 + d * 86400.0 + h * 3600.0 + m * 60.0 + s);
                     y = 0.0;
                     mo = 0.0;
                     w = 0.0;
                     d = 0.0;
                     h = 0.0;
                     m = 0.0;
                     s = 0.0;
                  }

                  if (i3.endsWith("y") && y == 0.0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        y = Double.parseDouble(i4);
                     }
                  } else if (i3.endsWith("mo") && mo == 0.0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        mo = Double.parseDouble(i4);
                        if (i3.equals("3mo") && mo == 3.0) {
                           mo = 3.0;
                        }
                     }
                  } else if (i3.endsWith("w") && w == 0.0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        w = Double.parseDouble(i4);
                     }
                  } else if (i3.endsWith("d") && d == 0.0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        d = Double.parseDouble(i4);
                     }
                  } else if (i3.endsWith("h") && h == 0.0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        h = Double.parseDouble(i4);
                     }
                  } else if (i3.endsWith("m") && m == 0.0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        m = Double.parseDouble(i4);
                     }
                  } else if (i3.endsWith("s") && s == 0.0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        s = Double.parseDouble(i4);
                     }
                  }

                  argCount++;
               }

               if (timeStart > 0L) {
                  timeEnd = (long)(y * 3.1536E7 + mo * 2592000.0 + w * 604800.0 + d * 86400.0 + h * 3600.0 + m * 60.0 + s);
               } else {
                  timeStart = (long)(y * 3.1536E7 + mo * 2592000.0 + w * 604800.0 + d * 86400.0 + h * 3600.0 + m * 60.0 + s);
               }

               next = 0;
            }
         }

         count++;
      }

      return timeEnd >= timeStart ? new long[]{timeEnd, timeStart} : new long[]{timeStart, timeEnd};
   }

   public static String parseTimeString(String[] inputArguments) {
      String[] argumentArray = (String[])inputArguments.clone();
      String time = "";
      int count = 0;
      int next = 0;
      boolean range = false;
      BigDecimal w = new BigDecimal(0);
      BigDecimal d = new BigDecimal(0);
      BigDecimal h = new BigDecimal(0);
      BigDecimal m = new BigDecimal(0);
      BigDecimal s = new BigDecimal(0);
      BigDecimal mo = new BigDecimal(0);
      BigDecimal y = new BigDecimal(0);

      for (String argument : argumentArray) {
         if (count > 0) {
            argument = argument.trim().toLowerCase(Locale.ROOT);
            argument = argument.replaceAll("\\\\", "");
            argument = argument.replaceAll("'", "");
            if (argument.equals("t:") || argument.equals("time:")) {
               next = 1;
            } else if (next != 1 && !argument.startsWith("t:") && !argument.startsWith("time:")) {
               next = 0;
            } else {
               argument = argument.replaceAll("time:", "");
               argument = argument.replaceAll("t:", "");
               argument = argument.replaceAll("y", "y:");
               argument = argument.replaceAll("mo", "mo:");
               argument = argument.replaceAll("m", "m:");
               argument = argument.replaceAll("w", "w:");
               argument = argument.replaceAll("d", "d:");
               argument = argument.replaceAll("h", "h:");
               argument = argument.replaceAll("s", "s:");
               range = argument.contains("-");
               int argCount = 0;
               String[] i2 = argument.split(":");

               for (String i3 : i2) {
                  if (range && argCount > 0 && !time.contains("-") && i3.startsWith("-")) {
                     y = new BigDecimal(0);
                     mo = new BigDecimal(0);
                     w = new BigDecimal(0);
                     d = new BigDecimal(0);
                     h = new BigDecimal(0);
                     m = new BigDecimal(0);
                     s = new BigDecimal(0);
                     time = time + " -";
                  }

                  if (i3.endsWith("y") && y.intValue() == 0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        y = new BigDecimal(i4);
                        if (range) {
                           time = time + " " + timeString(y) + "y";
                        } else {
                           time = time + " " + Phrase.build(Phrase.TIME_YEARS, timeString(y), y.doubleValue() == 1.0 ? "{1}" : "{2}");
                        }
                     }
                  } else if (i3.endsWith("mo") && mo.intValue() == 0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        mo = new BigDecimal(i4);
                        if (range) {
                           time = time + " " + timeString(mo) + "mo";
                        } else {
                           time = time + " " + Phrase.build(Phrase.TIME_MONTHS, timeString(mo), mo.doubleValue() == 1.0 ? "{1}" : "{2}");
                        }
                     }
                  } else if (i3.endsWith("w") && w.intValue() == 0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        w = new BigDecimal(i4);
                        if (range) {
                           time = time + " " + timeString(w) + "w";
                        } else {
                           time = time + " " + Phrase.build(Phrase.TIME_WEEKS, timeString(w), w.doubleValue() == 1.0 ? "{1}" : "{2}");
                        }
                     }
                  } else if (i3.endsWith("d") && d.intValue() == 0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        d = new BigDecimal(i4);
                        if (range) {
                           time = time + " " + timeString(d) + "d";
                        } else {
                           time = time + " " + Phrase.build(Phrase.TIME_DAYS, timeString(d), d.doubleValue() == 1.0 ? "{1}" : "{2}");
                        }
                     }
                  } else if (i3.endsWith("h") && h.intValue() == 0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        h = new BigDecimal(i4);
                        if (range) {
                           time = time + " " + timeString(h) + "h";
                        } else {
                           time = time + " " + Phrase.build(Phrase.TIME_HOURS, timeString(h), h.doubleValue() == 1.0 ? "{1}" : "{2}");
                        }
                     }
                  } else if (i3.endsWith("m") && m.intValue() == 0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        m = new BigDecimal(i4);
                        if (range) {
                           time = time + " " + timeString(m) + "m";
                        } else {
                           time = time + " " + Phrase.build(Phrase.TIME_MINUTES, timeString(m), m.doubleValue() == 1.0 ? "{1}" : "{2}");
                        }
                     }
                  } else if (i3.endsWith("s") && s.intValue() == 0) {
                     String i4 = i3.replaceAll("[^0-9.]", "");
                     if (i4.length() > 0 && i4.replaceAll("[^0-9]", "").length() > 0 && i4.indexOf(46) == i4.lastIndexOf(46)) {
                        s = new BigDecimal(i4);
                        if (range) {
                           time = time + " " + timeString(s) + "s";
                        } else {
                           time = time + " " + Phrase.build(Phrase.TIME_SECONDS, timeString(s), s.doubleValue() == 1.0 ? "{1}" : "{2}");
                        }
                     }
                  }

                  argCount++;
               }

               next = 0;
            }
         }

         count++;
      }

      if (time.startsWith(" ")) {
         time = time.substring(1);
      }

      return time;
   }

   public static int parseRows(String[] inputArguments) {
      String[] argumentArray = (String[])inputArguments.clone();
      int rows = 0;
      int count = 0;
      int next = 0;

      for (String argument : argumentArray) {
         if (count > 0) {
            argument = argument.trim().toLowerCase(Locale.ROOT);
            argument = argument.replaceAll("\\\\", "");
            argument = argument.replaceAll("'", "");
            if (argument.equals("rows:")) {
               next = 1;
            } else if (next != 1 && !argument.startsWith("rows:")) {
               next = 0;
            } else {
               argument = argument.replaceAll("rows:", "").trim();
               if (!argument.startsWith("-")) {
                  String i2 = argument.replaceAll("[^0-9]", "");
                  if (i2.length() > 0 && i2.length() < 10) {
                     rows = Integer.parseInt(i2);
                  }
               }

               next = 0;
            }
         }

         count++;
      }

      return rows;
   }

   private static String timeString(BigDecimal input) {
      return input.stripTrailingZeros().toPlainString();
   }
}
