package com.skyblockexp.ezrtp.config.effects;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.Locale;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;

public final class CountdownBossBarSettings {
   private final boolean enabled;
   private final String title;
   private final BarColor color;
   private final BarStyle style;

   public CountdownBossBarSettings(boolean enabled, String title, BarColor color, BarStyle style) {
      this.enabled = enabled;
      this.title = title;
      this.color = color;
      this.style = style;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public String getTitle() {
      return this.title;
   }

   public BarColor getColor() {
      return this.color;
   }

   public BarStyle getStyle() {
      return this.style;
   }

   public Component titleComponent(int seconds) {
      String resolved = this.title.replace("<seconds>", Integer.toString(Math.max(0, seconds)));
      return MessageUtil.parseMiniMessage(resolved);
   }

   public static CountdownBossBarSettings fromConfiguration(ConfigurationSection section, Logger logger) {
      if (section == null) {
         return disabled();
      } else {
         boolean enabled = section.getBoolean("enabled", false);
         String title = section.getString("title", "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>");
         String colorName = section.getString("color", "YELLOW");
         String styleName = section.getString("style", "SOLID");
         BarColor color = parseColor(colorName, logger);
         BarStyle style = parseStyle(styleName, logger);
         return new CountdownBossBarSettings(enabled, title, color, style);
      }
   }

   public static CountdownBossBarSettings disabled() {
      return new CountdownBossBarSettings(false, "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>", BarColor.YELLOW, BarStyle.SOLID);
   }

   private static BarColor parseColor(String name, Logger logger) {
      try {
         return BarColor.valueOf(name.trim().toUpperCase(Locale.ROOT));
      } catch (NullPointerException | IllegalArgumentException var3) {
         if (logger != null) {
            logger.warning("Invalid bossbar color: " + name);
         }

         return BarColor.YELLOW;
      }
   }

   private static BarStyle parseStyle(String name, Logger logger) {
      try {
         return BarStyle.valueOf(name.trim().toUpperCase(Locale.ROOT));
      } catch (NullPointerException | IllegalArgumentException var3) {
         if (logger != null) {
            logger.warning("Invalid bossbar style: " + name);
         }

         return BarStyle.SOLID;
      }
   }
}
