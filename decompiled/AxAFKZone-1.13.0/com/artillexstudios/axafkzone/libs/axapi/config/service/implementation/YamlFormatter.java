package com.artillexstudios.axafkzone.libs.axapi.config.service.implementation;

import com.artillexstudios.axafkzone.libs.axapi.config.service.Formatter;

public final class YamlFormatter implements Formatter {
   @Override
   public String format(String toFormat) {
      StringBuilder builder = new StringBuilder();
      String[] lines = toFormat.split("\n");

      for (int i = 0; i < lines.length; i++) {
         String line = lines[i];
         if (line.strip().startsWith("#")) {
            if (this.getLeadingWhiteSpace(line) == 0 && i >= 1) {
               builder.append("\n");
            }

            builder.append(this.toPrettyComment(line));
            builder.append("\n");

            for (int j = i + 1; j < lines.length; i++) {
               String nextLine = lines[j];
               if (!nextLine.strip().startsWith("#")) {
                  break;
               }

               builder.append(this.toPrettyComment(nextLine));
               builder.append("\n");
               j++;
            }
         } else if (i >= 1 && this.getLeadingWhiteSpace(line) < this.getLeadingWhiteSpace(lines[i - 1]) && this.getLeadingWhiteSpace(line) == 0) {
            builder.append("\n");
            builder.append(line);
            builder.append("\n");
         } else {
            builder.append(line);
            builder.append("\n");
         }
      }

      return builder.toString();
   }

   private int getLeadingWhiteSpace(String string) {
      int whiteSpace = 0;

      for (int i = 0; i < string.length(); i++) {
         char ch = string.charAt(i);
         if (!Character.isWhitespace(ch) && ch != '-') {
            break;
         }

         whiteSpace++;
      }

      return whiteSpace;
   }

   private String toPrettyComment(String string) {
      int index = string.indexOf(35);
      if (index != -1 && index != string.length() - 1) {
         char ch = string.charAt(index + 1);
         if (Character.isWhitespace(ch)) {
            return string;
         } else {
            StringBuilder builder = new StringBuilder(string.length());

            for (int i = 0; i < string.length(); i++) {
               if (string.charAt(i) == '#') {
                  builder.append('#').append(' ');
               } else {
                  builder.append(string.charAt(i));
               }
            }

            return builder.toString();
         }
      } else {
         return string;
      }
   }
}
