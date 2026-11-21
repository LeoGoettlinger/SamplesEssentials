package com.blaxk.spawnelytra.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public enum MessageUtil {
   private static final MiniMessage MM = MiniMessage.miniMessage();
   private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
   private static final Map<String, String> DEFAULT_MESSAGES = new HashMap<>();
   private static final Map<Character, String> SMALL_CAPS_MAP;
   private static final Pattern UPPERCASE_PLACEHOLDER_PATTERN = Pattern.compile("<([A-Za-z0-9_-]*[A-Z][A-Za-z0-9_-]*)>");
   private static final Map<String, String> messages = new HashMap<>();
   private static final Map<String, Boolean> messageToggles = new HashMap<>();
   private static Plugin plugin;
   private static BukkitAudiences audiences;
   private static boolean isPaperNativeAdventure = false;

   public static void initialize(Plugin plugin) {
      MessageUtil.plugin = plugin;

      try {
         Class.forName("io.papermc.paper.text.PaperComponents");
         isPaperNativeAdventure = true;
      } catch (ClassNotFoundException var2) {
         isPaperNativeAdventure = false;
      }

      if (!isPaperNativeAdventure) {
         audiences = BukkitAudiences.create(plugin);
      }
   }

   private static String canonicalizeLanguageCode(String input) {
      if (input == null) {
         return "en";
      } else {
         String s = input.trim().toLowerCase(Locale.ROOT);
         if (s.matches("^[a-z]{2}[-_][a-z]{2}$")) {
            s = s.substring(0, 2);
         }

         switch (s) {
            case "deutsch":
            case "german":
            case "de_de":
            case "de-at":
            case "de_at":
               return "de";
            case "français":
            case "francais":
            case "french":
            case "fr_fr":
               return "fr";
            case "español":
            case "espanol":
            case "spanish":
            case "es_es":
            case "es-mx":
               return "es";
            case "العربية":
            case "arabic":
            case "ar_sa":
            case "ar_eg":
               return "ar";
            case "english":
            case "en_us":
            case "en-gb":
               return "en";
            case "polski":
            case "polish":
            case "pl_pl":
               return "pl";
            default:
               return s.length() != 2 || !"en".equals(s) && !"de".equals(s) && !"es".equals(s) && !"fr".equals(s) && !"ar".equals(s) && !"pl".equals(s)
                  ? "en"
                  : s;
         }
      }
   }

   public static void shutdown() {
      if (audiences != null) {
         audiences.close();
         audiences = null;
      }
   }

   public static void loadMessages(Plugin plugin) {
      MessageUtil.plugin = plugin;
      FileConfiguration config = plugin.getConfig();
      String rawLanguage = config.getString("language", "en");
      String language = canonicalizeLanguageCode(rawLanguage);
      String rawStyle = config.getString("messages.style", "classic");
      String style = (rawStyle == null ? "classic" : rawStyle).toLowerCase(Locale.ROOT);
      messages.clear();
      DEFAULT_MESSAGES.forEach((key, value) -> messages.put(key, normalizePlaceholders(value)));
      Map<String, String> englishMessages = loadLanguageMessages(plugin, "en");
      englishMessages.forEach((key, value) -> messages.put(key, normalizePlaceholders(value)));
      Map<String, String> languageMessages = loadLanguageMessages(plugin, language);
      languageMessages.forEach((key, value) -> messages.put(key, normalizePlaceholders(value)));
      if ("small_caps".equals(style) && ("en".equals(language) || "de".equals(language))) {
         for (Entry<String, String> entry : new HashMap<>(messages).entrySet()) {
            String current = entry.getValue();
            if (current != null) {
               messages.put(entry.getKey(), toSmallCapsPreservingTags(current));
            }
         }
      }

      messageToggles.clear();
      messageToggles.put("press_to_boost", config.getBoolean("messages.show_press_to_boost", true));
      messageToggles.put("press_to_boost_bedrock", config.getBoolean("messages.show_press_to_boost", true));
      messageToggles.put("press_to_boost_remaining_bedrock", config.getBoolean("messages.show_press_to_boost", true));
      messageToggles.put("boost_activated", config.getBoolean("messages.show_boost_activated", true));
      messageToggles.put("creative_mode_elytra_disabled", config.getBoolean("messages.show_creative_disabled", false));
   }

   private static Map<String, String> loadLanguageMessages(Plugin plugin, String language) {
      Map<String, String> loaded = new HashMap<>();
      File langDir = new File(plugin.getDataFolder(), "lang");
      File langFile = new File(langDir, language + ".yml");
      FileConfiguration langConfig = null;
      if (langFile.exists()) {
         langConfig = YamlConfiguration.loadConfiguration(langFile);
      } else {
         try (InputStream defaultLangStream = plugin.getResource("lang/" + language + ".yml")) {
            if (defaultLangStream != null) {
               langConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultLangStream, StandardCharsets.UTF_8));
            }
         } catch (Exception var11) {
         }
      }

      if (langConfig == null && !"en".equals(language)) {
         return loadLanguageMessages(plugin, "en");
      } else if (langConfig == null) {
         return loaded;
      } else {
         for (String key : langConfig.getKeys(false)) {
            String value = langConfig.getString(key);
            if (value != null) {
               loaded.put(key, normalizePlaceholders(value));
            }
         }

         return loaded;
      }
   }

   private static String normalizePlaceholders(String input) {
      if (input == null) {
         return "";
      } else {
         Matcher matcher = UPPERCASE_PLACEHOLDER_PATTERN.matcher(input);
         StringBuilder buffer = new StringBuilder();

         while (matcher.find()) {
            String placeholder = matcher.group(1);
            String normalized = normalizePlaceholderName(placeholder);
            matcher.appendReplacement(buffer, "<" + normalized + ">");
         }

         matcher.appendTail(buffer);
         return buffer.toString();
      }
   }

   private static String normalizePlaceholderName(String name) {
      StringBuilder builder = new StringBuilder(name.length());
      char[] chars = name.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         char c = chars[i];
         if (Character.isUpperCase(c)) {
            if (i > 0 && '_' != chars[i - 1] && '-' != chars[i - 1]) {
               builder.append('_');
            }

            builder.append(Character.toLowerCase(c));
         } else {
            builder.append(Character.toLowerCase(c));
         }
      }

      return builder.toString();
   }

   public static String toSmallCapsPreservingTags(String input) {
      if (input != null && !input.isEmpty()) {
         StringBuilder out = new StringBuilder(input.length());
         boolean inTag = false;
         int depth = 0;

         for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if ('<' == ch) {
               inTag = true;
               depth++;
               out.append('<');
            } else if ('>' == ch && inTag) {
               out.append('>');
               if (--depth <= 0) {
                  inTag = false;
                  depth = 0;
               }
            } else if (inTag) {
               out.append(ch);
            } else {
               char lower = Character.toLowerCase(ch);
               String mapped = SMALL_CAPS_MAP.get(lower);
               if (mapped != null) {
                  out.append(mapped);
               } else {
                  out.append(ch);
               }
            }
         }

         return out.toString();
      } else {
         return input;
      }
   }

   public static Component component(String key, TagResolver... resolvers) {
      String raw = messages.getOrDefault(key, DEFAULT_MESSAGES.getOrDefault(key, key));
      return MM.deserialize(raw, resolvers);
   }

   public static String plain(String key, TagResolver... resolvers) {
      return PLAIN.serialize(component(key, resolvers));
   }

   public static void send(Player player, String key, TagResolver... resolvers) {
      if (player != null) {
         Component component = component(key, resolvers);
         if (isPaperNativeAdventure) {
            player.sendMessage(component);
         } else if (audiences != null) {
            audiences.player(player).sendMessage(component);
         } else {
            player.sendMessage(PLAIN.serialize(component));
         }
      }
   }

   public static void send(CommandSender sender, String key, TagResolver... resolvers) {
      if (sender != null) {
         Component component = component(key, resolvers);
         if (isPaperNativeAdventure) {
            sender.sendMessage(component);
         } else if (audiences != null) {
            audiences.sender(sender).sendMessage(component);
         } else {
            sender.sendMessage(PLAIN.serialize(component));
         }
      }
   }

   public static void sendActionBar(Player player, String key, TagResolver... resolvers) {
      if (messageToggles.getOrDefault(key, true)) {
         Component component = component(key, resolvers);
         if (isPaperNativeAdventure) {
            player.sendActionBar(component);
         } else if (audiences != null) {
            audiences.player(player).sendActionBar(component);
         }
      }
   }

   public static void sendRaw(Player player, Component component) {
      if (isPaperNativeAdventure) {
         player.sendMessage(component);
      } else if (audiences != null) {
         audiences.player(player).sendMessage(component);
      } else {
         player.sendMessage(PLAIN.serialize(component));
      }
   }

   public static void sendRaw(CommandSender sender, Component component) {
      if (isPaperNativeAdventure) {
         sender.sendMessage(component);
      } else if (audiences != null) {
         audiences.sender(sender).sendMessage(component);
      } else {
         sender.sendMessage(PLAIN.serialize(component));
      }
   }

   static {
      Map<Character, String> smallCaps = new HashMap<>();
      smallCaps.put('a', "ᴀ");
      smallCaps.put('b', "ʙ");
      smallCaps.put('c', "ᴄ");
      smallCaps.put('d', "ᴅ");
      smallCaps.put('e', "ᴇ");
      smallCaps.put('f', "ꜰ");
      smallCaps.put('g', "ɢ");
      smallCaps.put('h', "ʜ");
      smallCaps.put('i', "ɪ");
      smallCaps.put('j', "ᴊ");
      smallCaps.put('k', "ᴋ");
      smallCaps.put('l', "ʟ");
      smallCaps.put('m', "ᴍ");
      smallCaps.put('n', "ɴ");
      smallCaps.put('o', "ᴏ");
      smallCaps.put('p', "ᴘ");
      smallCaps.put('q', "ǫ");
      smallCaps.put('r', "ʀ");
      smallCaps.put('s', "ꜱ");
      smallCaps.put('t', "ᴛ");
      smallCaps.put('u', "ᴜ");
      smallCaps.put('v', "ᴠ");
      smallCaps.put('w', "ᴡ");
      smallCaps.put('x', "x");
      smallCaps.put('y', "ʏ");
      smallCaps.put('z', "ᴢ");
      smallCaps.put('ä', "ä");
      smallCaps.put('ö', "ö");
      smallCaps.put('ü', "ü");
      smallCaps.put('ß', "ꜱꜱ");
      SMALL_CAPS_MAP = Collections.unmodifiableMap(smallCaps);
      DEFAULT_MESSAGES.put("press_to_boost", "<#91f251>Press <bold><#74ea31><key:key.swapOffhand></bold> <#91f251>to boost yourself");
      DEFAULT_MESSAGES.put(
         "press_to_boost_remaining", "<#91f251>Press <bold><#74ea31><key:key.swapOffhand></bold> <#91f251>to boost <#ffd166>(<remaining> remaining)"
      );
      DEFAULT_MESSAGES.put("press_to_boost_bedrock", "<#91f251>Press <bold><#74ea31>Sneak</bold> <#91f251>to boost yourself");
      DEFAULT_MESSAGES.put("press_to_boost_remaining_bedrock", "<#91f251>Press <bold><#74ea31>Sneak</bold> <#91f251>to boost <#ffd166>(<remaining> remaining)");
      DEFAULT_MESSAGES.put("temp_elytra_lore", "<#aaa8a8>Temporary");
      DEFAULT_MESSAGES.put("boost_activated", "<#74ea31><bold>Boost activated!</bold>");
      DEFAULT_MESSAGES.put("boost_activated_remaining", "<#74ea31><bold>Boost activated!</bold> <#ffd166>(<remaining> remaining)");
      DEFAULT_MESSAGES.put("failed_update_check", "<#fd5e5e>Failed to check for updates: <error_message>");
      DEFAULT_MESSAGES.put("creative_mode_elytra_disabled", "<#ffeea2>Elytra flight disabled in Creative mode.");
      DEFAULT_MESSAGES.put("no_permission", "<#fd5e5e>You don't have permission to use this command.");
      DEFAULT_MESSAGES.put("command_player_only", "<#fd5e5e>This command can only be used by players.");
      DEFAULT_MESSAGES.put("reload_success", "<#91f251>SpawnElytra configuration reloaded.");
      DEFAULT_MESSAGES.put("spawnelytra_not_available", "<#fd5e5e>SpawnElytra instance not available.");
      DEFAULT_MESSAGES.put("help_header", "<#ffcc33>Spawn Elytra Help");
      DEFAULT_MESSAGES.put("help_reload", "<#fdba5e>/spawnelytra reload <#aaa8a8>- Reload the plugin configuration");
      DEFAULT_MESSAGES.put("help_info", "<#fdba5e>/spawnelytra info <#aaa8a8>- Show plugin information");
      DEFAULT_MESSAGES.put("help_update", "<#fdba5e>/spawnelytra update <#aaa8a8>- Automatically download and install the latest version");
      DEFAULT_MESSAGES.put("help_visualize", "<#fdba5e>/spawnelytra visualize <#aaa8a8>- Visualize the elytra area with particles");
      DEFAULT_MESSAGES.put("help_settings", "<#fdba5e>/spawnelytra settings <#aaa8a8>- Open the settings menu");
      DEFAULT_MESSAGES.put("info_header", "<#ffcc33>Spawn Elytra Config");
      DEFAULT_MESSAGES.put("info_version", "<#fdba5e>Version: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("info_website", "<#fdba5e>Website: <#5db3ff><value></#5db3ff>");
      DEFAULT_MESSAGES.put("info_world", "<#fdba5e>World: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("info_radius", "<#fdba5e>Radius: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("info_strength", "<#fdba5e>Boost Strength: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("info_boost_enabled", "<#fdba5e>Boost Enabled: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("info_activation_mode", "<#fdba5e>Activation Mode: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("info_offhand_key", "<#fdba5e>   Offhand Key: <bold><#fdba5e><key:key.swapOffhand></#fdba5e></bold>");
      DEFAULT_MESSAGES.put("info_f_key_launch_strength", "<#fdba5e>   F-Key Launch Strength: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("info_spawn_mode", "<#fdba5e>Spawn Mode: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("info_language", "<#fdba5e>Language: <#91f251><value></#91f251>");
      DEFAULT_MESSAGES.put("visualize_start", "<#91f251>Visualizing spawn area for <#ffd166><seconds></#ffd166> seconds...");
      DEFAULT_MESSAGES.put("visualize_end", "<#fdba5e>Area visualization ended.");
      DEFAULT_MESSAGES.put("visualize_stop", "<#fdba5e>Area visualization stopped.");
      DEFAULT_MESSAGES.put("visualize_no_area", "<#fd5e5e>No valid spawn area configured!");
      DEFAULT_MESSAGES.put("setup_started", "<#91f251>Setup Help enabled. Go to position <#ffd166>1</#ffd166> and run <#5db3ff>/se set pos1</#5db3ff>.");
      DEFAULT_MESSAGES.put("setup_already_running", "<#ffd166>Setup Help is already active.");
      DEFAULT_MESSAGES.put("setup_cancelled", "<#fdba5e>Setup Help exited.");
      DEFAULT_MESSAGES.put("setup_not_running", "<#fd5e5e>Setup Help is not active.");
      DEFAULT_MESSAGES.put("setup_pos1_set", "<#91f251>Position 1 set! Now go to position <#ffd166>2</#ffd166> and run <#5db3ff>/se set pos2</#5db3ff>.");
      DEFAULT_MESSAGES.put("setup_pos2_set", "<#91f251>Position 2 set! Previewing area...");
      DEFAULT_MESSAGES.put("setup_action_pos1", "<#5db3ff>Setup: Go to pos1 and run /se set pos1");
      DEFAULT_MESSAGES.put("setup_action_pos2", "<#5db3ff>Setup: Go to pos2 and run /se set pos2");
      DEFAULT_MESSAGES.put("setup_world_mismatch", "<#fd5e5e>Both positions must be in the same world.");
      DEFAULT_MESSAGES.put("setup_options_header", "<#ffcc33>Setup Options");
      DEFAULT_MESSAGES.put("setup_activation_mode_set", "<#91f251>Activation mode: <#ffd166><value></#ffd166>");
      DEFAULT_MESSAGES.put("setup_invalid_mode", "<#fd5e5e>Invalid activation mode.");
      DEFAULT_MESSAGES.put("setup_toggle_boost_label", "Boost activated hint");
      DEFAULT_MESSAGES.put("setup_toggle_press_label", "\"Press F\" hint");
      DEFAULT_MESSAGES.put("setup_toggled_boost_activated", "<#91f251>Boost activated hint: <#ffd166><value></#ffd166>");
      DEFAULT_MESSAGES.put("setup_toggled_press_to_boost", "<#91f251>\"Press F\" hint: <#ffd166><value></#ffd166>");
      DEFAULT_MESSAGES.put("setup_missing_positions", "<#fd5e5e>Please set both positions first.");
      DEFAULT_MESSAGES.put("setup_saved", "<#91f251>Setup saved and applied.");
      DEFAULT_MESSAGES.put("help_setup", "<#fdba5e>/spawnelytra setup <#aaa8a8>- Interactive Setup Help (pos1/pos2, options)");
   }
}
