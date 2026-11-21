package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;

public class SoundParser implements Parser {
   private static final Pattern SOUND_PATTERN = Pattern.compile("<sound:([A-Za-z0-9._-]+)(?::([0-9.]+))?(?::([0-9.]+))?>");
   private static final float DEFAULT_VOLUME = 1.0F;
   private static final float DEFAULT_PITCH = 1.0F;
   public static final SoundParser INSTANCE = new SoundParser();

   private SoundParser() {
   }

   @Override
   public String getType() {
      return "sound";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return string;
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType, Player player) {
      if (!string.contains("<sound:")) {
         return string;
      } else {
         Matcher soundMatcher = SOUND_PATTERN.matcher(string);
         if (!soundMatcher.find()) {
            return string;
         } else {
            soundMatcher.reset();

            while (soundMatcher.find()) {
               try {
                  String sound = soundMatcher.group(1).toLowerCase();
                  float volume = soundMatcher.group(2) != null ? Float.parseFloat(soundMatcher.group(2)) : 1.0F;
                  float pitch = soundMatcher.group(3) != null ? Float.parseFloat(soundMatcher.group(3)) : 1.0F;
                  Location playerLoc = player.getLocation();
                  player.playSound(playerLoc, sound, volume, pitch);
               } catch (IllegalArgumentException var9) {
               }
            }

            return soundMatcher.reset().replaceAll("");
         }
      }
   }
}
