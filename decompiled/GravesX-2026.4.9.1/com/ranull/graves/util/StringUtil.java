package com.ranull.graves.util;

import com.ranull.graves.Graves;
import com.ranull.graves.bundledlibraries.imdanix.text.MiniTranslator;
import com.ranull.graves.integration.MineDown;
import com.ranull.graves.libraries.commonstext.WordUtils;
import com.ranull.graves.type.Grave;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class StringUtil {
   private StringUtil() {
   }

   public static String format(String string) {
      return capitalizeFully(string.replace("_", " "));
   }

   public static String parseString(String string, Graves plugin) {
      return parseString(string, null, null, null, null, plugin);
   }

   public static String parseString(String string, Entity entity, Graves plugin) {
      return parseString(string, entity, null, null, null, plugin);
   }

   public static String parseString(String string, String name, Graves plugin) {
      return parseString(string, null, name, null, null, plugin);
   }

   public static String parseString(String string, Grave grave, Graves plugin) {
      return parseString(string, null, null, null, grave, plugin);
   }

   public static String parseString(String string, Location location, Grave grave, Graves plugin) {
      return parseString(string, null, null, location, grave, plugin);
   }

   public static String parseString(String string, Entity entity, Location location, Grave grave, Graves plugin) {
      return parseString(string, entity, plugin.getEntityManager().getEntityName(entity), location, grave, plugin);
   }

   public static String parseString(String string, Entity entity, String name, Location location, Grave grave, Graves plugin) {
      if (location != null) {
         string = string.replace("%world%", location.getWorld() != null ? location.getWorld().getName() : "")
            .replace("%x%", String.valueOf(location.getBlockX() + 0.5))
            .replace("%y%", String.valueOf(location.getBlockY() + 0.5))
            .replace("%z%", String.valueOf(location.getBlockZ() + 0.5));
         if (string.contains("%distance%") && entity != null) {
            if (entity.getWorld().equals(location.getWorld())) {
               string = string.replace("%distance%", String.valueOf(Math.round(entity.getLocation().distance(location))));
            } else {
               string = string.replace("%distance%", "Wrong dimension");
            }
         }
      }

      if (grave != null) {
         String var10000 = string.replace("%uuid%", grave.getUUID().toString())
            .replace("%owner_name%", grave.getOwnerName() != null ? grave.getOwnerName() : "")
            .replace(
               "%owner_name_display%",
               grave.getOwnerNameDisplay() != null ? grave.getOwnerNameDisplay() : (grave.getOwnerName() != null ? grave.getOwnerName() : "")
            )
            .replace("%owner_type%", grave.getOwnerType() != null ? grave.getOwnerType().name() : "")
            .replace("%owner_uuid%", grave.getOwnerUUID() != null ? grave.getOwnerUUID().toString() : "")
            .replace("%killer_name%", grave.getKillerName() != null ? grave.getKillerName() : "");
         String pattern = grave.getDeathCause();

         string = var10000.replace(
               "%killer_name_display%",
               switch (pattern) {
                  case "ENTITY_ATTACK", "ENTITY_SWEEP_ATTACK" -> grave.getKillerNameDisplay() != null
                     ? grave.getKillerNameDisplay()
                     : (grave.getKillerName() != null ? grave.getKillerName() : "Unknown");
                  default -> plugin.getGraveManager().getDamageReason(grave);
               }
            )
            .replace("%killer_death_cause%", grave.getDeathCause().replace("_", " ").toLowerCase())
            .replace("%killer_type%", grave.getKillerType() != null ? grave.getKillerType().name() : "")
            .replace("%killer_uuid%", grave.getKillerUUID() != null ? grave.getKillerUUID().toString() : "")
            .replace("%time_creation%", String.valueOf(grave.getTimeCreation()))
            .replace("%time_creation_formatted%", getDateString(grave, grave.getTimeCreation(), plugin))
            .replace("%time_alive_remaining%", String.valueOf(grave.getTimeAliveRemaining()))
            .replace("%time_alive_remaining_formatted%", getTimeString(grave, grave.getTimeAliveRemaining(), plugin))
            .replace("%time_protection_remaining%", String.valueOf(grave.getTimeProtectionRemaining()))
            .replace("%time_protection_remaining_formatted%", getTimeString(grave, grave.getTimeProtectionRemaining(), plugin))
            .replace("%time_lived%", String.valueOf(grave.getLivedTime()))
            .replace("%time_lived_formatted%", getTimeString(grave, grave.getLivedTime(), plugin))
            .replace(
               "%state_protection%",
               !grave.getProtection() || grave.getTimeProtectionRemaining() <= 0L && grave.getTimeProtectionRemaining() != -1L
                  ? plugin.getConfigManager().getConfigSection("protection.states.unprotected", grave).getString("protection.states.unprotected", "&c✘")
                  : plugin.getConfigManager().getConfigSection("protection.states.protected", grave).getString("protection.states.protected", "&a✔")
            )
            .replace("%state_abandoned%", String.valueOf(grave.isAbandoned()))
            .replace("%item%", String.valueOf(grave.getItemAmount()));
         if (grave.getExperience() > 0) {
            string = string.replace("%level%", String.valueOf(ExperienceUtil.getLevelFromExperience(grave.getExperience())))
               .replace("%experience%", String.valueOf(grave.getExperience()));
         } else {
            string = string.replace("%level%", "0").replace("%experience%", "0");
         }

         if (grave.getOwnerType() == EntityType.PLAYER && plugin.getIntegrationManager().hasPlaceholderAPI()) {
            string = PlaceholderAPI.setPlaceholders(plugin.getServer().getOfflinePlayer(grave.getOwnerUUID()), string);
         }
      }

      if (location != null && location.getWorld() != null && grave != null) {
         string = string.replace("%world_formatted%", location.getWorld() != null ? getWorldFormatted(location.getWorld().getName(), plugin) : "");
      } else {
         string = string.replace("%world_formatted%", "");
      }

      if (name != null) {
         string = string.replace("%name%", name).replace("%interact_name%", name).replace("%interact_type%", "null").replace("%interact_uuid%", "null");
      }

      if (entity != null) {
         string = string.replace("%interact_name%", plugin.getEntityManager().getEntityName(entity))
            .replace("%interact_type%", entity.getType().name())
            .replace("%interact_uuid%", entity.getUniqueId().toString());
      }

      string = string.replace("%plugin_name%", plugin.getName())
         .replace("%plugin_version%", plugin.getVersion())
         .replace("%spigot_id%", String.valueOf(plugin.getSpigotID()));
      if (plugin.getIntegrationManager().hasMineDown()) {
         string = MineDown.parseString(string);
      }

      Pattern pattern = Pattern.compile("&#[A-Fa-f0-9]{6}");

      for (Matcher matcher = pattern.matcher(string); matcher.find(); matcher = pattern.matcher(string)) {
         String colorHex = string.substring(matcher.start() + 1, matcher.end());
         string = plugin.getVersionManager().hasHexColors() ? string.replace("&" + colorHex, ChatColor.of(colorHex).toString()) : string.replace(colorHex, "");
      }

      if (plugin.getIntegrationManager().hasMiniMessage()) {
         string = string.replace("§", "&");
         return MiniTranslator.toMini(string);
      } else {
         return string.replace("&", "§");
      }
   }

   private static String getWorldFormatted(String worldName, Graves plugin) {
      for (String worldEntry : plugin.getConfigManager().getConfigSection("message.world", EntityType.PLAYER, null).getStringList("message.world")) {
         if (worldEntry.contains(worldName)) {
            try {
               String[] worldFinal = worldEntry.split(": ");
               return worldFinal[1];
            } catch (ArrayIndexOutOfBoundsException var6) {
            }
         }
      }

      return format(worldName);
   }

   @Deprecated
   public static String parseTime(String string, Grave grave) {
      long time = grave.getTimeCreation() - grave.getTimeAlive();
      int day = (int)TimeUnit.SECONDS.toDays(time);
      long hour = TimeUnit.SECONDS.toHours(time) - day * 24L;
      long minute = TimeUnit.SECONDS.toMinutes(time) - TimeUnit.SECONDS.toHours(time) * 60L;
      long second = TimeUnit.SECONDS.toSeconds(time) - TimeUnit.SECONDS.toMinutes(time) * 60L;
      if (day > 0) {
         string = string.replace("%day%", String.valueOf(day));
      }

      if (hour > 0L) {
         string = string.replace("%hour%", String.valueOf(hour));
      }

      if (minute > 0L) {
         string = string.replace("%minute%", String.valueOf(minute));
      }

      if (second > 0L) {
         string = string.replace("%second%", String.valueOf(second));
      }

      return string;
   }

   public static String getDateString(Grave grave, long time, Graves plugin) {
      return time > 0L
         ? new SimpleDateFormat(plugin.getConfigManager().getConfigSection("time.date", grave).getString("time.date", "dd-MM-yyyy")).format(new Date(time))
         : plugin.getConfigManager().getConfigSection("time.infinite", grave).getString("time.infinite");
   }

   public static String getTimeString(Grave grave, long time, Graves plugin) {
      if (time > 0L) {
         time /= 1000L;
         int day = (int)TimeUnit.SECONDS.toDays(time);
         long hour = TimeUnit.SECONDS.toHours(time) - day * 24L;
         long minute = TimeUnit.SECONDS.toMinutes(time) - TimeUnit.SECONDS.toHours(time) * 60L;
         long second = TimeUnit.SECONDS.toSeconds(time) - TimeUnit.SECONDS.toMinutes(time) * 60L;
         String timeDay = "";
         String timeHour = "";
         String timeMinute = "";
         String timeSecond = "";
         if (day > 0) {
            timeDay = plugin.getConfigManager().getConfigSection("time.day", grave).getString("time.day").replace("%day%", String.valueOf(day));
         }

         if (hour > 0L) {
            timeHour = plugin.getConfigManager().getConfigSection("time.hour", grave).getString("time.hour").replace("%hour%", String.valueOf(hour));
         }

         if (minute > 0L) {
            timeMinute = plugin.getConfigManager().getConfigSection("time.minute", grave).getString("time.minute").replace("%minute%", String.valueOf(minute));
         }

         if (second > 0L) {
            timeSecond = plugin.getConfigManager().getConfigSection("time.second", grave).getString("time.second").replace("%second%", String.valueOf(second));
         }

         return normalizeSpace(timeDay + timeHour + timeMinute + timeSecond);
      } else {
         return plugin.getConfigManager().getConfigSection("time.infinite", grave).getString("time.infinite");
      }
   }

   private static String normalizeSpace(String string) {
      try {
         string = StringUtils.normalizeSpace(string);
      } catch (NoClassDefFoundError var2) {
      }

      return string;
   }

   private static String capitalizeFully(String string) {
      try {
         string = WordUtils.capitalizeFully(string);
      } catch (NoClassDefFoundError var2) {
      }

      return string;
   }

   public static boolean isStringEmpty(String string) {
      return string == null || string.isEmpty();
   }
}
