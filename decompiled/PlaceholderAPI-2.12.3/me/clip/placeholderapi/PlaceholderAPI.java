package me.clip.placeholderapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import me.clip.placeholderapi.replacer.CharsReplacer;
import me.clip.placeholderapi.replacer.Replacer;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public final class PlaceholderAPI {
   private static final Replacer REPLACER_PERCENT = new CharsReplacer(Replacer.Closure.PERCENT);
   private static final Replacer REPLACER_BRACKET = new CharsReplacer(Replacer.Closure.BRACKET);
   static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");
   static final Pattern BRACKET_PLACEHOLDER_PATTERN = Pattern.compile("[{]([^{}]+)[}]");
   static final Pattern RELATIONAL_PLACEHOLDER_PATTERN = Pattern.compile("[%](rel_)([^%]+)[%]");

   private PlaceholderAPI() {
   }

   @NotNull
   public static String setPlaceholders(OfflinePlayer player, @NotNull String text) {
      return REPLACER_PERCENT.apply(text, player, PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion);
   }

   @NotNull
   public static List<String> setPlaceholders(OfflinePlayer player, @NotNull List<String> text) {
      List<String> result = new ArrayList<>(text.size());

      for (String line : text) {
         result.add(setPlaceholders(player, line));
      }

      return result;
   }

   @NotNull
   public static String setPlaceholders(Player player, @NotNull String text) {
      return setPlaceholders((OfflinePlayer)player, text);
   }

   @NotNull
   public static List<String> setPlaceholders(Player player, @NotNull List<String> text) {
      return setPlaceholders((OfflinePlayer)player, text);
   }

   @NotNull
   public static String setBracketPlaceholders(OfflinePlayer player, @NotNull String text) {
      return REPLACER_BRACKET.apply(text, player, PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion);
   }

   @NotNull
   public static List<String> setBracketPlaceholders(OfflinePlayer player, @NotNull List<String> text) {
      List<String> result = new ArrayList<>(text.size());

      for (String line : text) {
         result.add(setBracketPlaceholders(player, line));
      }

      return result;
   }

   @NotNull
   public static String setBracketPlaceholders(Player player, @NotNull String text) {
      return setBracketPlaceholders((OfflinePlayer)player, text);
   }

   @NotNull
   public static List<String> setBracketPlaceholders(Player player, @NotNull List<String> text) {
      return setBracketPlaceholders((OfflinePlayer)player, text);
   }

   public static String setRelationalPlaceholders(Player one, Player two, @NotNull String text) {
      Matcher matcher = RELATIONAL_PLACEHOLDER_PATTERN.matcher(text);

      while (matcher.find()) {
         String format = matcher.group(2);
         int index = format.indexOf(95);
         if (index > 0) {
            String identifier = format.substring(0, index).toLowerCase(Locale.ROOT);
            String params = format.substring(index + 1);
            PlaceholderExpansion expansion = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(identifier);
            if (expansion instanceof Relational) {
               String value = ((Relational)expansion).onPlaceholderRequest(one, two, params);
               if (value != null) {
                  text = text.replaceAll(Pattern.quote(matcher.group()), Matcher.quoteReplacement(value));
               }
            }
         }
      }

      return text;
   }

   public static List<String> setRelationalPlaceholders(Player one, Player two, @NotNull List<String> text) {
      List<String> result = new ArrayList<>(text.size());

      for (String line : text) {
         result.add(setRelationalPlaceholders(one, two, line));
      }

      return result;
   }

   public static boolean isRegistered(@NotNull String identifier) {
      return PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().findExpansionByIdentifier(identifier).isPresent();
   }

   @NotNull
   public static Set<String> getRegisteredIdentifiers() {
      return PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getIdentifiers();
   }

   public static Pattern getPlaceholderPattern() {
      return PLACEHOLDER_PATTERN;
   }

   public static Pattern getBracketPlaceholderPattern() {
      return BRACKET_PLACEHOLDER_PATTERN;
   }

   public static Pattern getRelationalPlaceholderPattern() {
      return RELATIONAL_PLACEHOLDER_PATTERN;
   }

   public static boolean containsPlaceholders(String text) {
      if (text == null) {
         return false;
      } else {
         int firstPercent = text.indexOf(37);
         return firstPercent == -1 ? false : text.indexOf(37, firstPercent + 1) != -1;
      }
   }

   public static boolean containsBracketPlaceholders(String text) {
      if (text == null) {
         return false;
      } else {
         int openBracket = text.indexOf(123);
         return openBracket == -1 ? false : text.indexOf(125, openBracket + 1) != -1;
      }
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static boolean registerExpansion(PlaceholderExpansion expansion) {
      return expansion.register();
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static boolean unregisterExpansion(PlaceholderExpansion expansion) {
      return expansion.unregister();
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static Map<String, PlaceholderHook> getPlaceholders() {
      return PlaceholderAPIPlugin.getInstance()
         .getLocalExpansionManager()
         .getExpansions()
         .stream()
         .collect(Collectors.toMap(PlaceholderExpansion::getIdentifier, ex -> (PlaceholderHook)ex));
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static boolean registerPlaceholderHook(Plugin plugin, PlaceholderHook placeholderHook) {
      Msg.warn(
         "Nag author(s) %s of plugin %s about their usage of the deprecated PlaceholderHook class! This class will be removed in v2.13.0!",
         plugin.getDescription().getAuthors(),
         plugin.getName()
      );
      return false;
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static boolean registerPlaceholderHook(String identifier, PlaceholderHook placeholderHook) {
      Msg.warn(
         "%s is attempting to register placeholders via deprecated PlaceholderHook class. This class is no longer supported and will be removed in v2.13.0!",
         identifier
      );
      return false;
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static boolean unregisterPlaceholderHook(Plugin plugin) {
      Msg.warn(
         "Nag author(s) %s of plugin %s about their usage of the PlaceholderAPI class. This way of unregistering placeholders is no longer supported and will be removed in v2.13.0!",
         plugin.getDescription().getAuthors(),
         plugin.getName()
      );
      return false;
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static boolean unregisterPlaceholderHook(String identifier) {
      Msg.warn(
         "%s is attempting to unregister placeholders via PlaceholderAPI class. This way of unregistering placeholders is no longer supported and will be removed in v2.13.0!",
         identifier
      );
      return false;
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static Set<String> getRegisteredPlaceholderPlugins() {
      return getRegisteredIdentifiers();
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static Set<String> getExternalPlaceholderPlugins() {
      return null;
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static String setPlaceholders(OfflinePlayer player, String text, Pattern pattern, boolean colorize) {
      return setPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static List<String> setPlaceholders(OfflinePlayer player, List<String> text, Pattern pattern, boolean colorize) {
      return setPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static List<String> setPlaceholders(OfflinePlayer player, List<String> text, boolean colorize) {
      return setPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static List<String> setPlaceholders(OfflinePlayer player, List<String> text, Pattern pattern) {
      return setPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static String setPlaceholders(Player player, String text, boolean colorize) {
      return setPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static List<String> setPlaceholders(Player player, List<String> text, boolean colorize) {
      return setPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static String setPlaceholders(OfflinePlayer player, String text, boolean colorize) {
      return setPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static String setPlaceholders(OfflinePlayer player, String text, Pattern pattern) {
      return setPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static List<String> setBracketPlaceholders(OfflinePlayer player, List<String> text, boolean colorize) {
      return setBracketPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static String setBracketPlaceholders(OfflinePlayer player, String text, boolean colorize) {
      return setBracketPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static String setBracketPlaceholders(Player player, String text, boolean colorize) {
      return setBracketPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static List<String> setBracketPlaceholders(Player player, List<String> text, boolean colorize) {
      return setBracketPlaceholders(player, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static String setRelationalPlaceholders(Player one, Player two, String text, boolean colorize) {
      return setRelationalPlaceholders(one, two, text);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public static List<String> setRelationalPlaceholders(Player one, Player two, List<String> text, boolean colorize) {
      return setRelationalPlaceholders(one, two, text);
   }
}
