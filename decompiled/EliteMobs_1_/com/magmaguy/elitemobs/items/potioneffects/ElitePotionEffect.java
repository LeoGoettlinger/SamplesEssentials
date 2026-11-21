package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfigFields;
import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ElitePotionEffect {
   private final PotionEffect potionEffect;
   private final ElitePotionEffect.Target target;
   private final ElitePotionEffect.ApplicationMethod applicationMethod;
   private final double value;

   public ElitePotionEffect(String definition) {
      if (definition == null) {
         throw new IllegalArgumentException("Missing potion definition");
      } else {
         String[] fields = definition.split(",", -1);
         if (fields.length >= 2 && fields.length <= 4) {
            String nativeName = LegacyValueConverter.parsePotionEffect(fields[0].trim()).toLowerCase(Locale.ROOT);
            PotionEffectType type = (PotionEffectType)Registry.EFFECT.get(new NamespacedKey("minecraft", nativeName));
            if (type == null) {
               throw new IllegalArgumentException("Unknown native potion effect: " + nativeName);
            } else {
               PotionEffectsConfigFields config = PotionEffectsConfig.getPotionEffect(type.getKey().getKey());
               if (config == null) {
                  throw new IllegalArgumentException("Native effect " + nativeName + " has no EliteMobs equipment-effect configuration");
               } else {
                  int amplifier = Integer.parseInt(fields[1].trim());
                  if (amplifier >= 0 && amplifier <= 255) {
                     ElitePotionEffect.Target target = fields.length >= 3
                        ? ElitePotionEffect.Target.valueOf(fields[2].trim().toUpperCase(Locale.ROOT))
                        : ElitePotionEffect.Target.SELF;
                     ElitePotionEffect.ApplicationMethod method = fields.length >= 4
                        ? ElitePotionEffect.ApplicationMethod.valueOf(fields[3].trim().toUpperCase(Locale.ROOT))
                        : ElitePotionEffect.ApplicationMethod.CONTINUOUS;
                     int duration = type.equals(PotionEffectType.NIGHT_VISION) ? 300 : 40;
                     if (method == ElitePotionEffect.ApplicationMethod.ONHIT) {
                        duration = Math.multiplyExact(config.getOnHitDuration(), 20);
                     }

                     if (duration > 0 && Double.isFinite(config.getValue()) && !(config.getValue() < 0.0)) {
                        this.potionEffect = new PotionEffect(type, duration, amplifier);
                        this.target = target;
                        this.applicationMethod = method;
                        this.value = config.getValue();
                     } else {
                        throw new IllegalArgumentException("Invalid potion duration or value for " + nativeName);
                     }
                  } else {
                     throw new IllegalArgumentException("Potion amplifier must be 0..255");
                  }
               }
            }
         } else {
            throw new IllegalArgumentException("Expected effect,amplifier[,target[,applicationMethod]]: " + definition);
         }
      }
   }

   public boolean isEnabled() {
      PotionEffectsConfigFields config = PotionEffectsConfig.getPotionEffect(this.potionEffect.getType().getKey().getKey());
      return config != null && config.isEnabled();
   }

   public PotionEffect getPotionEffect() {
      return this.potionEffect;
   }

   public ElitePotionEffect.Target getTarget() {
      return this.target;
   }

   public ElitePotionEffect.ApplicationMethod getApplicationMethod() {
      return this.applicationMethod;
   }

   public double getValue() {
      return this.value;
   }

   public static enum ApplicationMethod {
      ONHIT,
      CONTINUOUS;
   }

   public static enum Target {
      SELF,
      TARGET;
   }
}
