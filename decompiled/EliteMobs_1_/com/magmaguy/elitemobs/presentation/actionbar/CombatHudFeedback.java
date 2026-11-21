package com.magmaguy.elitemobs.presentation.actionbar;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

final class CombatHudFeedback {
   private static final boolean CUSTOM_SHADOW_SUPPORTED = supportsCustomShadow();
   private static final short[] METRICS = loadMetrics("/combat-hud-feedback-metrics.bin");
   private static final short[] CLASS_NAME_METRICS = loadMetrics("/combat-hud-class-name-metrics.bin");

   private CombatHudFeedback() {
   }

   private static boolean supportsCustomShadow() {
      try {
         BaseComponent.class.getMethod("setShadowColor", Color.class);
         return true;
      } catch (NoSuchMethodException var1) {
         return false;
      }
   }

   static void shadow(BaseComponent component, Color color) {
      if (CUSTOM_SHADOW_SUPPORTED) {
         component.setShadowColor(color);
      }
   }

   static BaseComponent[] components(String message, boolean legacy) {
      String singleLine = message.replace('\n', ' ').replace('\r', ' ');
      BaseComponent[] components = legacy ? TextComponent.fromLegacyText(singleLine) : new BaseComponent[]{new TextComponent(singleLine)};

      for (BaseComponent component : components) {
         component.setFont("elitemobs:combat_hud_feedback");
         shadow(component, new Color(0, 0, 0, 190));
      }

      return components;
   }

   static int widthInHalfPixels(BaseComponent[] components) {
      return widthInHalfPixels(components, METRICS);
   }

   static int classNameWidthInHalfPixels(BaseComponent[] components) {
      return widthInHalfPixels(components, CLASS_NAME_METRICS);
   }

   static BaseComponent[] className(String name) {
      String plain = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name)).replace('\n', ' ').replace('\r', ' ');
      int[] points = plain.codePoints().toArray();
      int width = 0;

      for (int point : points) {
         width += CLASS_NAME_METRICS[point] >> 2;
      }

      if (width > 268) {
         int suffixWidth = 3 * (CLASS_NAME_METRICS[46] >> 2);
         StringBuilder fitted = new StringBuilder();
         int used = 0;

         for (int point : points) {
            int advance = CLASS_NAME_METRICS[point] >> 2;
            if (used + advance + suffixWidth > 268) {
               break;
            }

            fitted.appendCodePoint(point);
            used += advance;
         }

         plain = fitted.append("...").toString();
      }

      TextComponent label = new TextComponent(plain);
      label.setFont("elitemobs:combat_hud_class_name");
      label.setColor(net.md_5.bungee.api.ChatColor.of("#fff0be"));
      shadow(label, new Color(0, true));
      return new BaseComponent[]{label};
   }

   private static int widthInHalfPixels(BaseComponent[] components, short[] metrics) {
      int width = 0;

      for (BaseComponent component : components) {
         int[] points = component.toPlainText().codePoints().toArray();

         for (int point : points) {
            int metric = metrics[point];
            width += (metric >> 2) + (component.isBold() ? metric & 3 : 0);
         }
      }

      return width;
   }

   private static short[] loadMetrics(String path) {
      short[] metrics = new short[1114112];
      Arrays.fill(metrics, (short)50);

      try (InputStream resource = CombatHudFeedback.class.getResourceAsStream(path)) {
         if (resource == null) {
            throw new IllegalStateException("Missing HUD feedback font metrics");
         } else {
            try (DataInputStream input = new DataInputStream(resource)) {
               if (input.readInt() != 1) {
                  throw new IOException("Unsupported HUD font metrics version");
               } else {
                  int count = input.readInt();
                  if (count >= 0 && count <= metrics.length) {
                     for (int i = 0; i < count; i++) {
                        int point = input.readInt();
                        if (!Character.isValidCodePoint(point)) {
                           throw new IOException("Invalid HUD glyph");
                        }

                        metrics[point] = (short)(input.readUnsignedByte() << 2 | input.readUnsignedByte());
                     }

                     return metrics;
                  } else {
                     throw new IOException("Invalid HUD glyph count");
                  }
               }
            }
         }
      } catch (IOException var11) {
         throw new IllegalStateException("Unable to load HUD feedback font metrics", var11);
      }
   }
}
