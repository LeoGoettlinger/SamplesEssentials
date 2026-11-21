package org.evenmorefish.fish.libs.daisylib.config.serializer;

import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PotionEffectSerializer implements ConfigSerializer<PotionEffect> {
   private static final PotionEffectSerializer INSTANCE = new PotionEffectSerializer();

   private PotionEffectSerializer() {
   }

   @NonNull
   public static PotionEffectSerializer get() {
      return INSTANCE;
   }

   @NonNull
   public String serialize(@NonNull PotionEffect element) {
      return element.getType().toString().toLowerCase() + "," + element.getAmplifier() + "," + element.getDuration();
   }

   @Nullable
   public PotionEffect deserialize(@Nullable String element) {
      if (element == null) {
         return null;
      } else if (element.contains(",")) {
         return this.deserialize(element, ",");
      } else {
         return element.contains(":") ? this.deserialize(element, ":") : null;
      }
   }

   @Nullable
   public PotionEffect deserialize(@Nullable String element, @NonNull String separator) {
      if (element == null) {
         return null;
      } else {
         String[] split = element.split(separator);
         if (split.length != 3) {
            DaisyLib.get().getLogging().error("Invalid potion effect string: " + element);
            DaisyLib.get().getLogging().error("The correct format is \"potion,amplifier,duration\".");
            return null;
         } else {
            NamespacedKey key = NamespacedKey.fromString(split[0].toLowerCase(Locale.ROOT));
            if (key == null) {
               DaisyLib.get().getLogging().warn("Could not fetch a key from " + split[0]);
               return null;
            } else {
               PotionEffectType type = (PotionEffectType)Registry.POTION_EFFECT_TYPE.get(key);
               if (type == null) {
                  DaisyLib.get().getLogging().error("Potion effect type " + split[0] + " is not valid.");
                  return null;
               } else {
                  int amplifier;
                  try {
                     amplifier = Integer.parseInt(split[1]);
                  } catch (NumberFormatException var10) {
                     DaisyLib.get().getLogging().error("Potion effect amplifier " + split[1] + " is not valid.");
                     return null;
                  }

                  int duration;
                  try {
                     duration = Integer.parseInt(split[2]);
                  } catch (NumberFormatException var9) {
                     DaisyLib.get().getLogging().error("Potion effect duration " + split[2] + " is not valid.");
                     return null;
                  }

                  return new PotionEffect(type, duration * 20, amplifier - 1, false);
               }
            }
         }
      }
   }
}
