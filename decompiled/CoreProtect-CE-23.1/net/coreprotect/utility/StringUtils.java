package net.coreprotect.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StringUtils {
   private StringUtils() {
      throw new IllegalStateException("Utility class");
   }

   public static String capitalize(String string, boolean allWords) {
      if (string != null && !string.isEmpty()) {
         if (string.length() <= 1) {
            return string.toUpperCase(Locale.ROOT);
         } else {
            string = string.toLowerCase(Locale.ROOT);
            if (allWords) {
               StringBuilder builder = new StringBuilder();

               for (String substring : string.split(" ")) {
                  if (substring.length() >= 3 && !substring.equals("and") && !substring.equals("the")) {
                     substring = substring.substring(0, 1).toUpperCase(Locale.ROOT) + substring.substring(1);
                  }

                  if (builder.length() > 0) {
                     builder.append(" ");
                  }

                  builder.append(substring);
               }

               return builder.toString();
            } else {
               return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1);
            }
         }
      } else {
         return string;
      }
   }

   public static String nameFilter(String name, int data) {
      if (name.equals("stone")) {
         switch (data) {
            case 1:
               name = "granite";
               break;
            case 2:
               name = "polished_granite";
               break;
            case 3:
               name = "diorite";
               break;
            case 4:
               name = "polished_diorite";
               break;
            case 5:
               name = "andesite";
               break;
            case 6:
               name = "polished_andesite";
               break;
            default:
               name = "stone";
         }
      }

      return name;
   }

   public static String[] toStringArray(String[] array) {
      int size = array.length;
      if (size == 11) {
         String time = array[0];
         String user = array[1];
         String x = array[2];
         String y = array[3];
         String z = array[4];
         String type = array[5];
         String data = array[6];
         String action = array[7];
         String rolledBack = array[8];
         String wid = array[9];
         String blockData = array[10];
         return new String[]{time, user, x, y, z, type, data, action, rolledBack, wid, "", "", blockData};
      } else {
         return null;
      }
   }

   public static String hoverCommandFilter(String string) {
      StringBuilder command = new StringBuilder();
      String[] data = string.toLowerCase().split(" ");
      if (data.length > 2) {
         if (data[1].equals("l")) {
            data[1] = "page";
         }

         if (data[2].startsWith("wid:")) {
            String nameWid = data[2].replaceFirst("wid:", "");
            if (nameWid.length() > 0 && nameWid.equals(nameWid.replaceAll("[^0-9]", ""))) {
               nameWid = WorldUtils.getWorldName(Integer.parseInt(nameWid));
               if (nameWid.length() > 0) {
                  data[2] = nameWid;
               }
            }
         }

         if (data[1].equals("teleport") && data.length > 5) {
            data[3] = Integer.toString((int)(Double.parseDouble(data[3]) - 0.5));
            data[4] = Integer.toString(Integer.parseInt(data[4]));
            data[5] = Integer.toString((int)(Double.parseDouble(data[5]) - 0.5));
         }
      }

      for (String s : data) {
         if (!s.isEmpty()) {
            if (command.length() > 0) {
               command.append(" ");
            }

            command.append(s);
         }
      }

      return command.toString();
   }

   public static Integer[] convertArray(String[] array) {
      List<Integer> list = new ArrayList<>();

      for (String item : array) {
         list.add(Integer.parseInt(item));
      }

      return list.toArray(new Integer[list.size()]);
   }
}
