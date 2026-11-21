package libs.com.ryderbelserion.vital.common.util;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class StringUtil {
   private StringUtil() {
      throw new AssertionError();
   }

   public static String formatInteger(int number) {
      return NumberFormat.getIntegerInstance(Locale.US).format((long)number);
   }

   public static String formatDouble(double number) {
      return NumberFormat.getNumberInstance(Locale.US).format(number);
   }

   public static String convertList(@NotNull List<String> list) {
      if (list.isEmpty()) {
         return "";
      } else {
         StringBuilder message = new StringBuilder();

         for (String line : list) {
            message.append(line).append("\n");
         }

         return chomp(message.toString());
      }
   }

   @NotNull
   public static String chomp(@NotNull String key) {
      if (key.isEmpty()) {
         return key;
      } else {
         char CR = '\r';
         char LF = '\n';
         if (key.length() == 1) {
            char ch = key.charAt(0);
            return ch != CR && ch != LF ? key : "";
         } else {
            int lastIdx = key.length() - 1;
            char last = key.charAt(lastIdx);
            if (last == LF) {
               if (key.charAt(lastIdx - 1) == CR) {
                  lastIdx--;
               }
            } else if (last != CR) {
               lastIdx++;
            }

            return key.substring(0, lastIdx);
         }
      }
   }

   public static Optional<Number> tryParseInt(@NotNull String value) {
      try {
         return Optional.of(Integer.parseInt(value));
      } catch (NumberFormatException var2) {
         return Optional.empty();
      }
   }

   public static Optional<Boolean> tryParseBoolean(@NotNull String value) {
      try {
         return Optional.of(Boolean.parseBoolean(value));
      } catch (NumberFormatException var2) {
         return Optional.empty();
      }
   }

   public static String getEnchant(@NotNull String enchant) {
      if (enchant.isEmpty()) {
         return "";
      } else {
         String var1 = enchant.toLowerCase();

         return switch (var1) {
            case "protection_environmental" -> "protection";
            case "protection_fire" -> "fire_protection";
            case "protection_fall" -> "feather_falling";
            case "protection_explosions" -> "blast_protection";
            case "protection_projectile" -> "projectile_protection";
            case "oxygen" -> "respiration";
            case "water_worker" -> "aqua_affinity";
            case "damage_all" -> "sharpness";
            case "damage_undead" -> "smite";
            case "damage_arthropods" -> "bane_of_arthropods";
            case "loot_bonus_mobs" -> "looting";
            case "dig_speed" -> "efficiency";
            case "durability" -> "unbreaking";
            case "loot_bonus_blocks" -> "fortune";
            case "arrow_damage" -> "power";
            case "arrow_knockback" -> "punch";
            case "arrow_fire" -> "flame";
            case "arrow_infinite" -> "infinity";
            case "luck" -> "luck_of_the_sea";
            default -> enchant.toLowerCase();
         };
      }
   }
}
