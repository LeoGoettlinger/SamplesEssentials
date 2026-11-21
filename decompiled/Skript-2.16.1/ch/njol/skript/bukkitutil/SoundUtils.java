package ch.njol.skript.bukkitutil;

import java.util.Locale;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SoundUtils {
   private static final boolean SOUND_IS_INTERFACE = Sound.class.isInterface();

   @Nullable
   public static NamespacedKey getKey(String soundString) {
      soundString = soundString.toUpperCase(Locale.ENGLISH);
      if (SOUND_IS_INTERFACE) {
         try {
            return Sound.valueOf(soundString).getKey();
         } catch (Exception var2) {
         }
      } else {
         try {
            Enum soundEnum = Enum.valueOf(Sound.class, soundString);
            return ((Keyed)soundEnum).getKey();
         } catch (IllegalArgumentException var3) {
         }
      }

      return NamespacedKey.fromString(soundString.toLowerCase(Locale.ENGLISH));
   }

   @NotNull
   public static NamespacedKey getKey(Sound sound) {
      return SOUND_IS_INTERFACE ? sound.getKey() : sound.getKey();
   }

   @Nullable
   public static Sound getSound(String soundString) {
      NamespacedKey key = getKey(soundString);
      return key == null ? null : (Sound)Registry.SOUNDS.get(key);
   }
}
