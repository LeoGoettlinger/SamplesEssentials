package com.viaversion.viaversion.libs.mcstructs.text.utils;

import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEvent;
import java.awt.Color;

public class TextComponentBuilder {
   public static TextComponent build(Object... parts) {
      StringComponent out = new StringComponent("");
      StringComponent current = null;
      Style style = new Style();

      for (Object part : parts) {
         if (part == null) {
            checkAppend(out, current, style);
            current = null;
            style = new Style();
         } else if (part instanceof TextFormatting) {
            style.setFormatting((TextFormatting)part);
         } else if (part instanceof Color) {
            style.setFormatting(new TextFormatting(((Color)part).getRGB()));
         } else if (part instanceof ClickEvent) {
            style.setClickEvent((ClickEvent)part);
         } else if (part instanceof HoverEvent) {
            style.setHoverEvent((HoverEvent)part);
         } else if (part instanceof Style) {
            style = (Style)part;
         } else if (part instanceof TextComponent) {
            if (checkAppend(out, current, style)) {
               current = null;
               style = new Style();
            }

            if (current == null) {
               current = (StringComponent)part;
            } else {
               current.append((TextComponent)part);
            }
         } else {
            if (checkAppend(out, current, style)) {
               current = null;
               style = new Style();
            }

            if (current == null) {
               current = new StringComponent(part.toString());
            } else {
               current.append(new String[]{part.toString()});
            }
         }
      }

      if (current != null) {
         if (!style.isEmpty()) {
            current.setStyle(style);
         }

         out.append(current);
      }

      return (TextComponent)(out.getSiblings().size() == 1 ? out.getSiblings().get(0) : out);
   }

   private static boolean checkAppend(TextComponent out, TextComponent current, Style style) {
      if (current == null) {
         return !style.isEmpty();
      } else if (style.isEmpty()) {
         return false;
      } else {
         out.append(current.setStyle(style));
         return true;
      }
   }
}
