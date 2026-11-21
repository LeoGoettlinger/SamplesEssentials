package com.oheers.fish.api.config.serializer;

import com.oheers.fish.api.Logging;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Builder;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SoundSerializer implements EMFSerializer<Sound> {
   private static final SoundSerializer INSTANCE = new SoundSerializer();

   private SoundSerializer() {
   }

   @NonNull
   public static SoundSerializer get() {
      return INSTANCE;
   }

   @NonNull
   public String serialize(@NonNull Sound element) {
      return element.name().asString() + "," + element.volume() + "," + element.pitch();
   }

   @Nullable
   public Sound deserialize(@Nullable String element) {
      return this.deserialize(element, 1.0F, 1.0F);
   }

   @Nullable
   public Sound deserialize(@Nullable String element, float defaultVolume, float defaultPitch) {
      if (element == null) {
         return null;
      } else {
         String[] split = element.split(",");
         if (split.length == 0) {
            return null;
         } else {
            Builder sound = resolveSoundType(split[0]);
            if (sound == null) {
               Logging.warn(split[0] + " is not a valid sound.");
               return null;
            } else {
               try {
                  sound.volume(Float.parseFloat(split[1]));
               } catch (ArrayIndexOutOfBoundsException | NumberFormatException var8) {
                  sound.volume(defaultVolume);
               }

               try {
                  sound.pitch(Float.parseFloat(split[2]));
               } catch (ArrayIndexOutOfBoundsException | NumberFormatException var7) {
                  sound.pitch(defaultPitch);
               }

               return (Sound)sound.build();
            }
         }
      }
   }

   @Nullable
   private static Builder resolveSoundType(@Nullable String type) {
      if (type == null) {
         return null;
      } else {
         Key soundKey = NamespacedKey.fromString(type);
         if (soundKey != null) {
            return Sound.sound().type(soundKey);
         } else {
            try {
               org.bukkit.Sound soundEnum = org.bukkit.Sound.valueOf(type);
               return Sound.sound().type(soundEnum);
            } catch (IllegalArgumentException var3) {
               return null;
            }
         }
      }
   }
}
