package net.coreprotect.utility;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.coreprotect.language.Phrase;
import org.bukkit.command.ConsoleCommandSender;

public class ChatUtils {
   private ChatUtils() {
      throw new IllegalStateException("Utility class");
   }

   public static String getCoordinates(String command, int worldId, int x, int y, int z, boolean displayWorld, boolean italic) {
      StringBuilder message = new StringBuilder("<COMPONENT>COMMAND");
      StringBuilder worldDisplay = new StringBuilder();
      if (displayWorld) {
         worldDisplay.append("/" + WorldUtils.getWorldName(worldId));
      }

      DecimalFormat decimalFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ROOT));
      message.append("|/" + command + " teleport wid:" + worldId + " " + decimalFormat.format(x + 0.5) + " " + y + " " + decimalFormat.format(z + 0.5) + "|");
      message.append(Color.GREY + (italic ? "§o" : "") + "(x" + x + "/y" + y + "/z" + z + worldDisplay.toString() + ")");
      return message.append("</COMPONENT>").toString();
   }

   public static String getPageNavigation(String command, int page, int totalPages) {
      StringBuilder message = new StringBuilder();
      String backArrow = "";
      if (page > 1) {
         backArrow = "◀ ";
         backArrow = "<COMPONENT>COMMAND|/" + command + " l " + (page - 1) + "|" + backArrow + "</COMPONENT>";
      }

      String nextArrow = " ";
      if (page < totalPages) {
         nextArrow = " ▶ ";
         nextArrow = "<COMPONENT>COMMAND|/" + command + " l " + (page + 1) + "|" + nextArrow + "</COMPONENT>";
      }

      StringBuilder pagination = new StringBuilder();
      if (totalPages > 1) {
         pagination.append(Color.GREY + "(");
         if (page > 3) {
            pagination.append(Color.WHITE + "<COMPONENT>COMMAND|/" + command + " l 1|1 </COMPONENT>");
            if (page > 4 && totalPages > 7) {
               pagination.append(Color.GREY + "... ");
            } else {
               pagination.append(Color.GREY + "| ");
            }
         }

         int displayStart = page - 2 < 1 ? 1 : page - 2;
         int displayEnd = page + 2 > totalPages ? totalPages : page + 2;
         if (page > 999 || page > 101 && totalPages > 99999) {
            displayStart = displayStart + 1 < displayEnd ? displayStart + 1 : displayStart;
            displayEnd = displayEnd - 1 > displayStart ? displayEnd - 1 : displayEnd;
            if (displayStart > totalPages - 3) {
               displayStart = totalPages - 3 < 1 ? 1 : totalPages - 3;
            }
         } else {
            if (displayStart > totalPages - 5) {
               displayStart = totalPages - 5 < 1 ? 1 : totalPages - 5;
            }

            if (displayEnd < 6) {
               displayEnd = 6 > totalPages ? totalPages : 6;
            }
         }

         if (page > 99999) {
            displayStart = displayStart + 1 < displayEnd ? displayStart + 1 : displayStart;
            displayEnd = displayEnd - 1 >= displayStart ? displayEnd - 1 : displayEnd;
            if (page == totalPages - 1) {
               displayEnd = totalPages - 1;
            }

            if (displayStart < displayEnd) {
               displayStart = displayEnd;
            }
         }

         if (page > 3 && displayStart == 1) {
            displayStart = 2;
         }

         for (int displayPage = displayStart; displayPage <= displayEnd; displayPage++) {
            if (page != displayPage) {
               pagination.append(
                  Color.WHITE
                     + "<COMPONENT>COMMAND|/"
                     + command
                     + " l "
                     + displayPage
                     + "|"
                     + displayPage
                     + (displayPage < totalPages ? " " : "")
                     + "</COMPONENT>"
               );
            } else {
               pagination.append(Color.WHITE + "§n" + displayPage + "§r" + (displayPage < totalPages ? " " : ""));
            }

            if (displayPage < displayEnd) {
               pagination.append(Color.GREY + "| ");
            }
         }

         if (displayEnd < totalPages) {
            if (displayEnd < totalPages - 1) {
               pagination.append(Color.GREY + "... ");
            } else {
               pagination.append(Color.GREY + "| ");
            }

            if (page != totalPages) {
               pagination.append(Color.WHITE + "<COMPONENT>COMMAND|/" + command + " l " + totalPages + "|" + totalPages + "</COMPONENT>");
            } else {
               pagination.append(Color.WHITE + "§n" + totalPages);
            }
         }

         pagination.append(Color.GREY + ")");
      }

      return message.append(
            Color.WHITE + backArrow + Color.DARK_AQUA + Phrase.build(Phrase.LOOKUP_PAGE, Color.WHITE + page + "/" + totalPages) + nextArrow + pagination
         )
         .toString();
   }

   public static String getTimeSince(long resultTime, long currentTime, boolean component) {
      StringBuilder message = new StringBuilder();
      double timeSince = currentTime - (resultTime + 0.0);
      if (timeSince < 0.0) {
         timeSince = 0.0;
      }

      DecimalFormat decimalFormat = new DecimalFormat("0.00");
      timeSince /= 60.0;
      if (timeSince < 60.0) {
         message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + Phrase.build(Phrase.TIME_UNITS, "{1}")));
      }

      if (message.length() == 0) {
         timeSince /= 60.0;
         if (timeSince < 24.0) {
            message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + Phrase.build(Phrase.TIME_UNITS, "{2}")));
         }
      }

      if (message.length() == 0) {
         timeSince /= 24.0;
         message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + Phrase.build(Phrase.TIME_UNITS, "{3}")));
      }

      if (component) {
         Date logDate = new Date(resultTime * 1000L);
         String formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(logDate);
         return "<COMPONENT>POPUP|" + Color.GREY + formattedTimestamp + "|" + Color.GREY + message.toString() + "</COMPONENT>";
      } else {
         return message.toString();
      }
   }

   public static String createTooltip(String phrase, String tooltip) {
      if (tooltip.isEmpty()) {
         return phrase;
      } else {
         StringBuilder message = new StringBuilder("<COMPONENT>POPUP");
         message.append("|" + tooltip.replace("|", "<PIPE/>") + "|");
         message.append(phrase);
         return message.append("</COMPONENT>").toString();
      }
   }

   public static void sendConsoleComponentStartup(ConsoleCommandSender consoleSender, String string) {
      Chat.sendComponent(consoleSender, "§r[CoreProtect] " + string + "<COMPONENT>POPUP| | </COMPONENT>");
   }
}
