package me.usainsrht.announcement.utils;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class CenterUtil {
   public static int getWidth(char c) {
      return switch (c) {
         case ' ', '"', '(', ')', '*', 'I', '[', ']', 't', '{', '}', 'İ' -> 3;
         case '!', '\'', ',', '.', ':', ';', 'i', '|', 'ı' -> 1;
         case '<', '>', 'f', 'k' -> 4;
         case '@' -> 6;
         case '`', 'l' -> 2;
         default -> 5;
      };
   }

   public static int getWidth(String text) {
      Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
      return getComponentWidth(component);
   }

   public static String centeredText(String text, int length, char centerChar) {
      int compensated = 0;
      StringBuilder centered = new StringBuilder();
      int width = getWidth(text);

      while (compensated < (length - width) / 2) {
         centered.append(centerChar);
         compensated += getWidth(centerChar) + 1;
      }

      return centered + text;
   }

   public static int getComponentWidth(Component component) {
      int width = 0;
      String plain = PlainTextComponentSerializer.plainText().serialize(component);
      char[] chars = new char[plain.length()];
      plain.getChars(0, plain.length(), chars, 0);

      for (char c : chars) {
         width += getWidth(c) + 1;
      }

      for (Component child : getBolds(component)) {
         width += PlainTextComponentSerializer.plainText().serialize(child).length();
      }

      return width;
   }

   public static List<Component> getBolds(Component component) {
      List<Component> newList = new ArrayList<>();
      if (component.hasDecoration(TextDecoration.BOLD)) {
         newList.add(component);
      } else {
         for (Component child : component.children()) {
            if (child.hasDecoration(TextDecoration.BOLD)) {
               newList.add(child);
            }
         }
      }

      return newList;
   }

   public static Component centeredComponent(Component component, int length, char centerChar) {
      int compensated = 0;
      int width = getComponentWidth(component);
      int centerCharWidth = getWidth(centerChar) + 1;

      StringBuilder centered;
      for (centered = new StringBuilder(); compensated < (length - width) / 2; compensated += centerCharWidth) {
         centered.append(centerChar);
      }

      return Component.text(centered.toString()).append(component);
   }
}
