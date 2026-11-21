package su.nightexpress.nightcore.util;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.regex.TimedMatcher;

@Deprecated
public class Colorizer {
   @Deprecated
   public static final Pattern PATTERN_HEX = Pattern.compile("#([A-Fa-f0-9]{6})");

   @Deprecated
   @NonNull
   public static String apply(@NonNull String str) {
      return hex(legacy(str));
   }

   @Deprecated
   @NonNull
   public static List<String> apply(@NonNull List<String> list) {
      list.replaceAll(Colorizer::apply);
      return list;
   }

   @Deprecated
   @NonNull
   public static Set<String> apply(@NonNull Set<String> set) {
      return set.stream().map(Colorizer::apply).collect(Collectors.toSet());
   }

   @Deprecated
   @NonNull
   public static String legacyHex(@NonNull String str) {
      return hex(legacy(str));
   }

   @Deprecated
   @NonNull
   public static String legacy(@NonNull String str) {
      return ChatColor.translateAlternateColorCodes('&', str);
   }

   @Deprecated
   @NonNull
   public static String hex(@NonNull String str) {
      TimedMatcher timedMatcher = TimedMatcher.create(PATTERN_HEX, str);
      StringBuilder buffer = new StringBuilder(str.length() + 32);

      while (timedMatcher.find()) {
         Matcher matcher = timedMatcher.getMatcher();
         String group = matcher.group(1);
         matcher.appendReplacement(
            buffer,
            "§x§" + group.charAt(0) + "§" + group.charAt(1) + "§" + group.charAt(2) + "§" + group.charAt(3) + "§" + group.charAt(4) + "§" + group.charAt(5)
         );
      }

      return timedMatcher.getMatcher().appendTail(buffer).toString();
   }

   @Deprecated
   @NonNull
   public static String plain(@NonNull String str) {
      return LegacyColors.plainColors(str);
   }

   @Deprecated
   @NonNull
   public static String plainLegacy(@NonNull String str) {
      return LegacyColors.plainDefaultColors(str);
   }

   @Deprecated
   @NonNull
   public static String plainHex(@NonNull String str) {
      return LegacyColors.plainHEXColors(str);
   }

   @Deprecated
   @NonNull
   public static String strip(@NonNull String str) {
      String stripped = ChatColor.stripColor(str);
      return stripped == null ? "" : stripped;
   }

   @Deprecated
   @NonNull
   public static String restrip(@NonNull String str) {
      return strip(apply(str));
   }
}
