package me.chancesd.pvpmanager.setting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.stream.Stream;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.sdutils.utils.TimeUtil;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import me.chancesd.pvpmanager.setting.lang.Replacement;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum Lang implements TimeUtil.TimeLangProvider {
   PREFIX("Prefix", "&#992222&lPvP &#FF5555➤"),
   OTHER_STATUS_ENABLED("Other_Status_Enabled", Replacement.PLAYER),
   OTHERS_STATUS_DISABLED("Others_Status_Disabled", Replacement.PLAYER),
   PVPDISABLED("PvP_Disabled"),
   PVPENABLED("PvP_Enabled"),
   SELF_STATUS_DISABLED("Self_Status_Disabled"),
   SELF_STATUS_ENABLED("Self_Status_Enabled"),
   COMMAND_DENIED_INCOMBAT("Command_Denied_InCombat"),
   ATTACK_DENIED_YOU("Attack_Denied_You"),
   ATTACK_DENIED_OTHER("Attack_Denied_Other", Replacement.PLAYER),
   DUEL_MODE_PROTECTION("Duel_Mode_Protection"),
   PVP_DISABLED_FEE("PvP_Disabled_Fee", Replacement.MONEY),
   PVP_FEE_NOT_ENOUGH("PvP_Disabled_Fee_Not_Enough"),
   TAGGED_ATTACKER("Tagged_Attacker", Replacement.PLAYER),
   TAGGED_DEFENDER("Tagged_Defender", Replacement.PLAYER),
   OUT_OF_COMBAT("Out_Of_Combat"),
   OUT_OF_COMBAT_ACTIONBAR("Out_Of_Combat_ActionBar"),
   NEWBIE_PROTECTION("Newbie_Protection", Replacement.TIME),
   NEWBIE_PROTECTION_END("Newbie_Protection_End"),
   NEWBIE_PROTECTION_REMOVED("Newbie_Protection_Removed"),
   NEWBIE_PROTECTION_ON_HIT("Newbie_Protection_On_Hit"),
   NEWBIE_PROTECTION_ATTACKER("Newbie_Protection_Attacker", Replacement.PLAYER),
   NEWBIE_TIME_CHECK("Newbie_Time_Check", Replacement.TIME),
   NEWBIE_TIME_CHECK_OTHER("Newbie_Time_Check_Other", Replacement.PLAYER, Replacement.TIME),
   NEWBIE_COMMAND_BLOCKED("Newbie_Command_Blocked"),
   NEWBIE_PICKUP_ITEM_BLOCKED("Newbie_Pickup_Items_Blocked"),
   NEWBIE_FORCE_REMOVED_WG("Newbie_Force_Removed_WorldGuard"),
   BLOCK_PLACE_BLOCKED_IN_COMBAT("Block_Place_Blocked_InCombat"),
   BLOCK_BREAK_BLOCKED_IN_COMBAT("Block_Break_Blocked_InCombat"),
   EAT_BLOCKED_IN_COMBAT("Eating_Blocked_InCombat"),
   ELYTRA_BLOCKED_IN_COMBAT("Elytra_Blocked_InCombat"),
   ENDERPEARL_BLOCKED_INCOMBAT("EnderPearl_Blocked_InCombat"),
   CHORUS_BLOCKED_IN_COMBAT("ChorusFruit_Blocked_InCombat"),
   INTERACT_BLOCKED_IN_COMBAT("Interact_Blocked_InCombat"),
   TELEPORT_BLOCKED_IN_COMBAT("Teleport_Blocked_InCombat"),
   TOTEM_BLOCKED_IN_COMBAT("Totem_Blocked_InCombat"),
   INVENTORY_BLOCKED_IN_COMBAT("Inventory_Blocked_InCombat"),
   FIREWORK_BLOCKED_IN_COMBAT("Firework_Blocked_InCombat"),
   FIREWORK_POWER_LIMITED_IN_COMBAT("Firework_Power_Limited_InCombat", Replacement.POWER),
   PUSHBACK_WARNING("Pushback_Warning"),
   ERROR_NOT_NEWBIE("Error_Not_Newbie"),
   ERROR_PVP_COOLDOWN("Error_PvP_Cooldown", Replacement.TIME),
   ERROR_PVP_TOGGLE_NO_PVP("Error_PvPToggle_NoPvP"),
   ERROR_PVP_TOGGLE_FORCE_PVP("Error_PvPToggle_ForcePvP"),
   ERROR_PERMISSION("Error_Permission"),
   ERROR_NOT_PLAYER("Error_Not_Player"),
   MONEY_REWARD("Money_Reward", Replacement.VICTIM, Replacement.MONEY),
   MONEY_PENALTY("Money_Penalty", Replacement.MONEY),
   MONEY_STEAL("Money_Steal", Replacement.PLAYER, Replacement.MONEY),
   EXP_WON("Exp_Won", Replacement.VICTIM, Replacement.EXP),
   EXP_STOLEN("Exp_Stolen", Replacement.PLAYER, Replacement.EXP),
   PVP_LIST_TITLE("PvPList_Title"),
   PVP_LIST_ENABLED("PvPList_Enabled"),
   PVP_LIST_DISABLED("PvPList_Disabled"),
   PVP_LIST_NO_RESULTS("PvPList_Nothing_Found"),
   PVP_TOGGLE_ADMIN_CHANGED("PvPToggle_Admin_Changed", Replacement.PLAYER, Replacement.STATE),
   PVP_TOGGLE_ALREADY_DISABLED("PvPToggle_Already_Disabled"),
   PVP_TOGGLE_ALREADY_ENABLED("PvPToggle_Already_Enabled"),
   ERROR_PLAYER_NOT_FOUND("Error_Player_Not_Found", Replacement.PLAYER),
   TAG_TIME_LEFT("Tag_Timeleft", Replacement.TIME),
   TAG_NOT_IN_COMBAT("Tag_Not_In_Combat"),
   RESPAWN_PROTECTION_SELF("Respawn_Protection"),
   RESPAWN_PROTECTION_OTHER("Respawn_Protection_Other", Replacement.PLAYER),
   WORLD_PROTECTION("World_Protection"),
   AFK_PROTECTION("AFK_Protection"),
   GLOBAL_PROTECTION("Global_Protection"),
   PVP_FORCE_ENABLED_WG("PvP_Force_Enabled_WorldGuard"),
   ENABLED("Enabled"),
   DISABLED("Disabled"),
   KILL_ABUSE_WARNING("Kill_Abuse_Warning"),
   TIME_DAYS("Time_Days"),
   TIME_HOURS("Time_Hours"),
   TIME_MINUTES("Time_Minutes"),
   TIME_SECONDS("Time_Seconds"),
   TIME_NOW("Time_Now"),
   ITEM_COOLDOWN("Item_Cooldown", Replacement.TIME);

   private static final String LOCALE_FOLDER = "locale/";
   private static final String ACTIONBAR_PREFIX = "!actionbar ";
   private static final String CHAT_PREFIX = "!chat ";
   private static PvPManager plugin;
   private static final Properties LANG_PROPERTIES = new Properties();
   private static final Queue<String> messageQueue = new LinkedList<>();
   private static File messagesFile;
   private static Locale locale;
   private final String messageKey;
   private final Replacement[] replacements;
   private String message;
   private DisplayMode displayMode = DisplayMode.CHAT;

   private Lang(final String messageKey, final Replacement... replacements) {
      this.messageKey = messageKey;
      this.replacements = replacements;
   }

   private Lang(final String messageKey, final String defaultMessage) {
      this(messageKey);
      this.message = ChatUtils.colorize(defaultMessage);
   }

   private Lang(final String messageKey) {
      this(messageKey);
   }

   public void loadMessage() {
      String[] parsed = parseMessageWithMode(this.messageKey);
      this.message = parsed[0];
      this.displayMode = DisplayMode.valueOf(parsed[1]);
   }

   @NotNull
   public String msg() {
      return this.message;
   }

   public Replacement[] getReplacements() {
      return this.replacements;
   }

   public String getMessageKey() {
      return this.messageKey;
   }

   public DisplayMode getDisplayMode() {
      return this.displayMode;
   }

   @NotNull
   public String msg(String... arguments) {
      String finalMessage = this.message;

      for (int i = 0; i < this.replacements.length && i < arguments.length; i++) {
         String placeholder = this.replacements[i].getPlaceholder();
         finalMessage = finalMessage.replace(placeholder, arguments[i]);
      }

      return finalMessage;
   }

   @NotNull
   public String msg(Object... arguments) {
      String finalMessage = this.message;

      for (int i = 0; i < this.replacements.length && i < arguments.length; i++) {
         String placeholder = this.replacements[i].getPlaceholder();
         String replacement = arguments[i] == null ? "null" : arguments[i].toString();
         finalMessage = finalMessage.replace(placeholder, replacement);
      }

      return finalMessage;
   }

   @NotNull
   public String msgTimeUntil(long time) {
      return this.message.replace(Replacement.TIME.getPlaceholder(), TimeUtil.getDiffUntil(this, time));
   }

   @NotNull
   public String msgTime(long time) {
      return this.message.replace(Replacement.TIME.getPlaceholder(), TimeUtil.getDiffDuration(this, time));
   }

   public static void setup(PvPManager plugin) {
      Lang.plugin = plugin;
      String localeString = Conf.LOCALE.asString();

      try {
         locale = Locale.valueOf(localeString);
      } catch (IllegalArgumentException var3) {
         Log.warning("Error! Locale '" + localeString + "' does not exist! Using default messages");
         locale = Locale.EN;
      }

      load();
   }

   private static void load() {
      messagesFile = new File(plugin.getDataFolder(), locale.fileName());
      checkForVersionUpgrade();
      if (!messagesFile.exists()) {
         createMessagesFile();
      }

      try (InputStreamReader in = new InputStreamReader(new FileInputStream(messagesFile), StandardCharsets.UTF_8)) {
         if (messagesFile.exists()) {
            LANG_PROPERTIES.clear();
            LANG_PROPERTIES.load(in);
            checkChanges();
            migrateProtectionMessages();
            Stream.of(values()).forEach(Lang::loadMessage);
         }
      } catch (IOException var5) {
         Log.severe("Error reading locale file", var5);
      }
   }

   private static void checkForVersionUpgrade() {
      if (plugin.getConfigM().isMajorVersionUpgrade()) {
         String fileName = locale.fileName();
         Utils.renameFile(messagesFile, fileName.substring(0, fileName.length() - 11) + "_v3_old.properties");
      }
   }

   public static void deletePreviousMessageFile() {
      File[] listFiles = plugin.getDataFolder().listFiles();
      if (listFiles != null) {
         for (File file : listFiles) {
            String fileName = file.getName();
            if (fileName.contains("messages") && !fileName.equalsIgnoreCase(locale.fileName()) && !fileName.contains("old")) {
               try {
                  Files.delete(file.toPath());
               } catch (IOException var7) {
                  Log.warning("Failed to delete messages file", var7);
               }
            }
         }
      }
   }

   private static void createMessagesFile() {
      byte[] buffer = new byte[4096];

      try {
         label85: {
            try (
               InputStream input = plugin.getResource("locale/" + locale.fileName());
               OutputStream resStreamOut = new FileOutputStream(plugin.getDataFolder() + File.separator + locale.fileName());
            ) {
               if (input != null) {
                  while (true) {
                     int readBytes;
                     if ((readBytes = input.read(buffer)) == -1) {
                        break label85;
                     }

                     resStreamOut.write(buffer, 0, readBytes);
                  }
               }

               Log.severe("Couldn't find the default locale file " + locale.fileName());
            }

            return;
         }
      } catch (IOException var10) {
         Log.severe("Error reading default locale from jar", var10);
      }

      Log.infoColor(ChatColor.DARK_GREEN + "New messages file created successfully!");
   }

   @NotNull
   public static String getString(String key) {
      String message = LANG_PROPERTIES.getProperty(key);
      return ChatUtils.colorize(message).replace(Replacement.PREFIX.getPlaceholder(), PREFIX.msg());
   }

   @NotNull
   private static String[] parseMessageWithMode(String key) {
      String message = getString(key);
      DisplayMode mode = DisplayMode.CHAT;
      if (message.toLowerCase().startsWith("!actionbar ")) {
         if (MCVersion.isAtLeast(MCVersion.V1_10)) {
            mode = DisplayMode.ACTION_BAR;
         }

         message = message.substring("!actionbar ".length());
      } else if (message.toLowerCase().startsWith("!chat ")) {
         message = message.substring("!chat ".length());
      }

      return new String[]{message, mode.name()};
   }

   private static Properties loadPropertiesFromStream(InputStream inputStream) {
      Properties props = new Properties();

      try (InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
         props.load(in);
      } catch (IOException var7) {
         Log.severe("Error loading properties from stream", var7);
      }

      return props;
   }

   private static void checkChanges() {
      try (
         InputStream inputStreamEN = plugin.getResource("locale/" + Locale.EN.fileName());
         InputStream inputStream = plugin.getResource("locale/" + locale.fileName());
      ) {
         Properties originalEN = loadPropertiesFromStream(inputStreamEN);
         Properties original = loadPropertiesFromStream(inputStream);
         Enumeration<Object> originalKeys = originalEN.keys();

         while (originalKeys.hasMoreElements()) {
            String a = (String)originalKeys.nextElement();
            if (!LANG_PROPERTIES.containsKey(a)) {
               Log.info("Added missing '" + a + "' key to messages file.");
               String newProperty = original.getProperty(a) != null ? original.getProperty(a) : originalEN.getProperty(a);
               String messageToAdd = a + " = " + newProperty;
               addMessage(messageToAdd);
               LANG_PROPERTIES.setProperty(a, newProperty);
            }
         }
      } catch (IOException var12) {
         Log.severe(var12.getMessage(), var12);
      }
   }

   private static void addMessage(String messageToAdd) {
      String messageKey = messageToAdd.split(" = ")[0];
      String escapedMessage = messageToAdd.replace("\n", "\\n");
      String previousKey = null;

      try (
         InputStream inputStream = plugin.getResource("locale/" + Locale.EN.fileName());
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      ) {
         String line;
         while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.contains(" = ") && !line.startsWith("#")) {
               String currentKey = line.split(" = ")[0];
               if (currentKey.equals(messageKey)) {
                  while (previousKey != null && !LANG_PROPERTIES.containsKey(previousKey)) {
                     previousKey = findKeyBefore(previousKey);
                  }
                  break;
               }

               previousKey = currentKey;
            }
         }

         if (previousKey != null && LANG_PROPERTIES.containsKey(previousKey)) {
            List<String> lines = Files.readAllLines(messagesFile.toPath(), StandardCharsets.UTF_8);

            for (int i = 0; i < lines.size(); i++) {
               if (lines.get(i).trim().startsWith(previousKey + " = ")) {
                  lines.add(i + 1, escapedMessage);
                  Files.write(messagesFile.toPath(), lines, StandardCharsets.UTF_8);
                  return;
               }
            }
         }
      } catch (IOException var16) {
         Log.severe(var16.getMessage(), var16);
      }

      try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(messagesFile, true), StandardCharsets.UTF_8))) {
         pw.println(escapedMessage);
      } catch (IOException var11) {
         Log.severe(var11.getMessage(), var11);
      }
   }

   private static String findKeyBefore(String targetKey) {
      try (
         InputStream inputStream = plugin.getResource("locale/" + Locale.EN.fileName());
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      ) {
         String previousKey = null;

         String line;
         while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.contains(" = ") && !line.startsWith("#")) {
               String currentKey = line.split(" = ")[0];
               if (currentKey.equals(targetKey)) {
                  return previousKey;
               }

               previousKey = currentKey;
            }
         }

         return null;
      } catch (IOException var11) {
         Log.severe(var11.getMessage(), var11);
         return null;
      }
   }

   private static void migrateProtectionMessages() {
      int configVersion = plugin.getConfigM().getOldVersion();
      if (configVersion < 205) {
         String[] protectionKeys = new String[]{
            "Respawn_Protection",
            "Respawn_Protection_Other",
            "World_Protection",
            "AFK_Protection",
            "Global_Protection",
            "Newbie_Protection_Attacker",
            "Attack_Denied_You",
            "Attack_Denied_Other",
            "Block_Place_Blocked_InCombat",
            "Block_Break_Blocked_InCombat",
            "Eating_Blocked_InCombat",
            "Elytra_Blocked_InCombat",
            "Firework_Blocked_InCombat",
            "Firework_Power_Limited_InCombat",
            "Interact_Blocked_InCombat",
            "Newbie_Pickup_Items_Blocked",
            "Inventory_Blocked_InCombat",
            "Item_Cooldown"
         };
         String[] timeKeys = new String[]{"Time_Days", "Time_Hours", "Time_Minutes", "Time_Seconds"};
         boolean modified = false;

         for (String key : protectionKeys) {
            String value = LANG_PROPERTIES.getProperty(key);
            if (value != null && !value.toLowerCase().startsWith("!actionbar ") && !value.toLowerCase().startsWith("!chat ")) {
               LANG_PROPERTIES.setProperty(key, "!actionbar " + value);
               modified = true;
            }
         }

         for (String keyx : timeKeys) {
            String value = LANG_PROPERTIES.getProperty(keyx);
            if (value != null && !value.startsWith(" ") && !value.equals("now")) {
               LANG_PROPERTIES.setProperty(keyx, " " + value);
               modified = true;
            }
         }

         if (modified) {
            try {
               List<String> lines = Files.readAllLines(messagesFile.toPath(), StandardCharsets.UTF_8);
               List<String> updatedLines = new LinkedList<>();
               boolean documentationAdded = false;

               for (String line : lines) {
                  String updatedLine = line;
                  if (!documentationAdded && line.trim().startsWith("Prefix = ")) {
                     updatedLines.add("# You can prefix any message with !actionbar or !chat to control where it appears");
                     updatedLines.add("# - !actionbar = Message shows above hotbar (recommended for frequent spam messages)");
                     updatedLines.add("# - !chat = Message shows in chat (default if no prefix)");
                     updatedLines.add("# Example: Item_Cooldown = !actionbar {prefix} &cYou can't use this yet! Available in &e{time}");
                     updatedLines.add("");
                     documentationAdded = true;
                  }

                  for (String keyxx : protectionKeys) {
                     if (line.trim().startsWith(keyxx + " = ")) {
                        String newValue = LANG_PROPERTIES.getProperty(keyxx);
                        updatedLine = keyxx + " = " + newValue;
                        break;
                     }
                  }

                  for (String keyxxx : timeKeys) {
                     if (line.trim().startsWith(keyxxx + " = ")) {
                        String newValue = LANG_PROPERTIES.getProperty(keyxxx);
                        updatedLine = keyxxx + " = \\" + newValue;
                        break;
                     }
                  }

                  updatedLines.add(updatedLine);
               }

               Files.write(messagesFile.toPath(), updatedLines, StandardCharsets.UTF_8);
               Log.infoColor(ChatColor.AQUA + "Migrated messages to use action bar display mode and added spacing to time units");
            } catch (IOException var15) {
               Log.warning("Failed to save migrated protection messages", var15);
            }
         }
      }
   }

   public static void messageProtection(ProtectionResult result, Player player, Player attacked) {
      CombatPlayer receiver = plugin.getPlayerManager().get(player);
      Lang message = getProtectionMessage(result);
      if (message != null) {
         if (result.isAttacker()) {
            receiver.messageWithDuration(message, 500L, new Object[0]);
         } else {
            receiver.messageWithDuration(message, 500L, new Object[]{attacked.getName()});
         }
      }
   }

   public static Lang getProtectionMessage(ProtectionResult result) {
      return switch (result.type()) {
         case NEWBIE -> result.isAttacker() ? NEWBIE_PROTECTION_ON_HIT : NEWBIE_PROTECTION_ATTACKER;
         case PVPDISABLED -> result.isAttacker() ? ATTACK_DENIED_YOU : ATTACK_DENIED_OTHER;
         case RESPAWN_PROTECTION -> result.isAttacker() ? RESPAWN_PROTECTION_SELF : RESPAWN_PROTECTION_OTHER;
         case WORLD_PROTECTION -> WORLD_PROTECTION;
         case AFK_PROTECTION -> AFK_PROTECTION;
         case GLOBAL_PROTECTION -> GLOBAL_PROTECTION;
         case DUEL_PROTECTION -> DUEL_MODE_PROTECTION;
         default -> null;
      };
   }

   @Override
   public String getTime(ChronoUnit time) {
      return switch (time) {
         case DAYS -> TIME_DAYS.msg();
         case HOURS -> TIME_HOURS.msg();
         case MINUTES -> TIME_MINUTES.msg();
         case SECONDS -> TIME_SECONDS.msg();
         default -> TIME_NOW.msg();
      };
   }

   public static Locale getLocale() {
      return locale;
   }

   public static void sendQueuedMsgs(CombatPlayer player) {
      messageQueue.forEach(msg -> player.getPlayer().sendMessage(msg));
   }

   public static void queueAdminMsg(String message) {
      if (!messageQueue.contains(message)) {
         messageQueue.add(message);
      }
   }

   @Override
   public String toString() {
      return this.message;
   }
}
