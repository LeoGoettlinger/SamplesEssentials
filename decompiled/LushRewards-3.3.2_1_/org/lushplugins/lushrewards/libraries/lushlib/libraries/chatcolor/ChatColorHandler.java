package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor;

import java.util.Collection;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.LegacyMessenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.Messenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.MiniMessageMessenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.ParserTypes;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parsers;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.HexParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.LegacyHexParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.LegacySpigotParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.MiniMessageColorParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.MiniMessageInteractionParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.MiniMessagePlaceholderParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.MiniMessageResolverParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.MiniMessageTextFormattingParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.MiniPlaceholdersParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.ParticleParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.PlaceholderAPIParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.SoundParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom.SpigotParser;

public class ChatColorHandler {
   private static boolean initialised = false;
   private static Messenger messenger;
   private static final Parsers parsers = new Parsers();
   private static final ChatColorHandlerSettings settings = new ChatColorHandlerSettings();

   public static String translate(@Nullable String string) {
      return translate(string, settings.defaultParsers());
   }

   public static String translate(@Nullable String string, @NotNull List<Parser> parsers) {
      return translate(string, null, parsers);
   }

   public static String translate(@Nullable String string, Player player) {
      return translate(string, player, settings.defaultParsers());
   }

   public static String translate(@Nullable String string, Player player, List<Parser> parsers) {
      ensureInitialised();
      return string != null && !string.isBlank() ? ChatColorHandler.parsers.parseString(string, Parser.OutputType.SPIGOT, player, parsers) : "";
   }

   public static List<String> translate(@NotNull Collection<String> strings) {
      return translate(strings, settings.defaultParsers());
   }

   public static List<String> translate(@NotNull Collection<String> strings, List<Parser> parsers) {
      return translate(strings, null, parsers);
   }

   public static List<String> translate(@NotNull Collection<String> strings, Player player) {
      return translate(strings, player, settings.defaultParsers());
   }

   public static List<String> translate(@NotNull Collection<String> strings, Player player, List<Parser> parsers) {
      return strings.stream().map(string -> translate(string, player, parsers)).toList();
   }

   public static String stripColor(@Nullable String string) {
      ensureInitialised();
      return string != null && !string.isBlank() ? ChatColor.stripColor(translate(string, ParserTypes.all())) : "";
   }

   public static void sendMessage(@NotNull CommandSender recipient, @Nullable String message) {
      messenger().sendMessage(recipient, message);
   }

   public static void sendMessage(@NotNull CommandSender recipient, @Nullable String... messages) {
      messenger().sendMessage(recipient, messages);
   }

   public static void sendMessage(CommandSender[] recipients, @Nullable String message) {
      messenger().sendMessage(recipients, message);
   }

   public static void sendMessage(CommandSender[] recipients, @Nullable String... messages) {
      messenger().sendMessage(recipients, messages);
   }

   public static void broadcastMessage(@Nullable String message) {
      messenger().broadcastMessage(message);
   }

   public static void broadcastMessage(@NotNull String... messages) {
      messenger().broadcastMessage(messages);
   }

   public static void sendActionBarMessage(@NotNull Player player, @Nullable String message) {
      messenger().sendActionBarMessage(player, message);
   }

   public static void sendActionBarMessage(@NotNull Player[] players, @Nullable String message) {
      messenger().sendActionBarMessage(players, message);
   }

   public static void sendTitle(@NotNull Player player, @Nullable String title) {
      messenger().sendTitle(player, title);
   }

   public static void sendTitle(@NotNull Player player, @Nullable String title, @Nullable String subtitle) {
      messenger().sendTitle(player, title, subtitle);
   }

   public static void sendTitle(@NotNull Player player, @Nullable String title, @Nullable String subtitle, int fadeIn, int fadeOut) {
      messenger().sendTitle(player, title, subtitle, fadeIn, fadeOut);
   }

   public static void sendTitle(@NotNull Player player, @Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
      messenger().sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
   }

   public static void sendTitle(@NotNull Player[] players, @Nullable String title) {
      messenger().sendTitle(players, title);
   }

   public static void sendTitle(@NotNull Player[] players, @Nullable String title, @Nullable String subtitle) {
      messenger().sendTitle(players, title, subtitle);
   }

   public static void sendTitle(@NotNull Player[] players, @Nullable String title, @Nullable String subtitle, int fadeIn, int fadeOut) {
      messenger().sendTitle(players, title, subtitle, fadeIn, fadeOut);
   }

   public static void sendTitle(@NotNull Player[] players, @Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
      messenger().sendTitle(players, title, subtitle, fadeIn, stay, fadeOut);
   }

   public static Messenger messenger() {
      ensureInitialised();
      return messenger;
   }

   public static void messenger(Messenger messenger) {
      ChatColorHandler.messenger = messenger;
   }

   public static Parsers parsers() {
      ensureInitialised();
      return parsers;
   }

   public static ChatColorHandlerSettings settings() {
      return settings;
   }

   protected static void debugLog(String log) {
      if (settings.debug()) {
         Bukkit.getLogger().info("[ChatColorHandler] " + log);
      }
   }

   public static synchronized void ensureInitialised() {
      if (!initialised) {
         init();
      }
   }

   private static void init() {
      initialised = true;
      parsers.register(HexParser.INSTANCE, 83);
      parsers.register(SpigotParser.INSTANCE, 65);
      parsers.register(SoundParser.INSTANCE, 60);
      parsers.register(ParticleParser.INSTANCE, 59);
      if (isPaper()) {
         messenger(new MiniMessageMessenger());
         parsers.register(MiniMessageResolverParser.INSTANCE, 89);
         parsers.register(LegacyHexParser.INSTANCE, 75);
         parsers.register(LegacySpigotParser.INSTANCE, 74);
         parsers.register(MiniMessageColorParser.INSTANCE, 73);
         parsers.register(MiniMessageInteractionParser.INSTANCE, 72);
         parsers.register(MiniMessagePlaceholderParser.INSTANCE, 71);
         parsers.register(MiniMessageTextFormattingParser.INSTANCE, 70);
         debugLog("Found MiniMessage in Server. MiniMessage support enabled.");
      } else {
         messenger(new LegacyMessenger());
         debugLog("Unable to find MiniMessage. MiniMessage support not enabled.");
      }

      PluginManager pluginManager = Bukkit.getServer().getPluginManager();
      if (pluginManager.getPlugin("PlaceholderAPI") != null && pluginManager.isPluginEnabled("PlaceholderAPI")) {
         parsers.register(PlaceholderAPIParser.INSTANCE, 90);
         debugLog("Found plugin \"PlaceholderAPI\". PlaceholderAPI support enabled.");
      }

      if (pluginManager.getPlugin("MiniPlaceholders") != null && pluginManager.isPluginEnabled("MiniPlaceholders")) {
         parsers.register(MiniPlaceholdersParser.INSTANCE, -1);
         debugLog("Found plugin \"MiniPlaceholders\". MiniPlaceholders support enabled.");
      }

      settings().defaultParsers(ParserTypes.all());
   }

   private static boolean hasClass(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   private static boolean isPaper() {
      if (hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration")) {
         try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage").getMethod("miniMessage");
            return true;
         } catch (NoSuchMethodException | ClassNotFoundException var1) {
         }
      }

      return false;
   }

   private ChatColorHandler() {
   }
}
