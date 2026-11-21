package net.coreprotect.utility;

import org.bukkit.ChatColor;

public class ChatMessage {
   String pluginName = Color.DARK_AQUA + "CoreProtect";
   String message;
   String textColor = Color.WHITE;
   String textStyle = "";
   String separator = "-";
   boolean useTag = true;
   boolean useSpaces = true;

   public ChatMessage() {
      this.message = "";
   }

   public ChatMessage setSeparator(String separator) {
      this.separator = separator;
      return this;
   }

   public ChatMessage setColor(String color) {
      this.textColor = color;
      return this;
   }

   public ChatMessage setStyle(String style) {
      this.textStyle = style;
      return this;
   }

   public ChatMessage useTag(boolean tag) {
      this.useTag = tag;
      return this;
   }

   public ChatMessage useSpaces(boolean spaces) {
      this.useSpaces = spaces;
      return this;
   }

   public ChatMessage append(String color, String string) {
      this.message = this.message + color + string;
      return this;
   }

   public ChatMessage(String string) {
      this.message = parseQuotes(string, this.textColor);
   }

   public String build(boolean tag) {
      this.useTag(tag);
      return this.build();
   }

   public String build(String color) {
      this.setColor(color);
      return this.build();
   }

   public String build(boolean tag, String color) {
      this.useTag(tag);
      this.setColor(color);
      return this.build();
   }

   public String build(String color, String style) {
      this.setColor(color);
      this.setStyle(style);
      return this.build();
   }

   public String build(boolean tag, String color, String style) {
      this.useTag(tag);
      this.setColor(color);
      this.setStyle(style);
      return this.build();
   }

   public String build(String separator, String color, String style) {
      this.setSeparator(separator);
      this.setColor(color);
      this.setStyle(style);
      return this.build();
   }

   public String build(boolean tag, String separator, String color, String style) {
      this.useTag(tag);
      this.setSeparator(separator);
      this.setColor(color);
      this.setStyle(style);
      return this.build();
   }

   public static String parseQuotes(String string, String textColor) {
      int indexFirst = string.indexOf("\"");
      int indexLast = string.lastIndexOf("\"");
      if (indexFirst > -1 && indexLast > indexFirst) {
         String quoteText = string.substring(indexFirst + 1, indexLast);
         string = string.replace(quoteText, Color.DARK_AQUA + ChatColor.stripColor(quoteText) + textColor);
      }

      return string;
   }

   private static String createSpaces(String string, boolean seperatorOffset, boolean createSpaces) {
      String result = "";
      if (!createSpaces) {
         return result;
      } else {
         int count = (string.length() - string.replace(String.valueOf('§'), "").length()) * 2;
         int length = (int)((string.length() - count) * 1.4);
         if (seperatorOffset) {
            length += 2;
         }

         for (int i = 0; i < length; i++) {
            result = result + " ";
         }

         return result;
      }
   }

   public String build() {
      return (this.useTag ? this.pluginName : createSpaces(this.pluginName, true, this.useSpaces))
         + this.textColor
         + " "
         + this.separator
         + " "
         + this.textStyle
         + this.message;
   }

   @Override
   public String toString() {
      return this.message;
   }
}
