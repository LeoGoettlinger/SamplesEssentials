package de.oliver.fancynpcs.libs.chatcolorhandler.common.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SoundParser implements Parser {
   public static final SoundParser INSTANCE = new SoundParser(60);
   private static final Pattern SOUND_PATTERN = Pattern.compile("<sound:([A-Za-z0-9._-]+)(?::([0-9.]+))?(?::([0-9.]+))?>");
   private static final float DEFAULT_VOLUME = 1.0F;
   private static final float DEFAULT_PITCH = 1.0F;
   private final int priority;

   public SoundParser(int priority) {
      this.priority = priority;
   }

   @Override
   public ParserType getType() {
      return ParserType.SOUND;
   }

   @Override
   public int getPriority() {
      return this.priority;
   }

   @Override
   public String parseString(String string, @Nullable Player player) {
      if (player != null && string.contains("<sound:")) {
         Matcher soundMatcher = SOUND_PATTERN.matcher(string);
         if (!soundMatcher.lookingAt()) {
            return string;
         } else {
            while (soundMatcher.find()) {
               try {
                  String sound = soundMatcher.group(1).toLowerCase();
                  float volume = soundMatcher.group(2) != null ? Float.parseFloat(soundMatcher.group(2)) : 1.0F;
                  float pitch = soundMatcher.group(3) != null ? Float.parseFloat(soundMatcher.group(3)) : 1.0F;
                  Location playerLoc = player.getLocation();
                  player.playSound(playerLoc, sound, volume, pitch);
               } catch (IllegalArgumentException var8) {
               }
            }

            return soundMatcher.reset().replaceAll("");
         }
      } else {
         return string;
      }
   }
}
