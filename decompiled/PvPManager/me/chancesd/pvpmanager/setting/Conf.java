package me.chancesd.pvpmanager.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Conf {
   LOCALE("General Settings", "Locale", "EN", String.class),
   USE_TEAMS("General Settings", "Use Scoreboard Teams", true, Boolean.class),
   WORLD_EXCLUSIONS("General Settings", "World Exclusions", Conf::getSet, List.class, Set.class),
   COMBAT_TAG_ENABLED("Combat Tag", "Enabled", true, Boolean.class),
   TIME_IN_COMBAT("Combat Tag", "Time", 20, Integer.class),
   GLOWING_IN_COMBAT("Combat Tag", "Glowing", true, Boolean.class),
   UNTAG_ON_KILL("Combat Tag", "Untag On Kill", false, Boolean.class),
   SELF_TAG("Combat Tag", "Self Tag", false, Boolean.class),
   PEARL_RENEW_TAG("Combat Tag", "EnderPearl Renews Tag", true, Boolean.class),
   WIND_CHARGE_RENEW_TAG("Combat Tag", "WindCharge Renews Tag", bool -> MCVersion.isAtLeast(MCVersion.V1_21) && bool, Boolean.class, Boolean.class),
   CLOSE_INVENTORY_ON_TAG("Combat Tag", "Close Inventory On Tag", true, Boolean.class),
   DUEL_MODE_ENABLED("Combat Tag", "Duel Mode", false, Boolean.class),
   NAMETAG_COMBAT_ENABLED("Combat Tag.Display.Nametags", "Enabled", true, Boolean.class),
   NAMETAG_PREFIX("Combat Tag.Display.Nametags", "Prefix", "&4&lCombat &c", String.class),
   NAMETAG_SUFFIX("Combat Tag.Display.Nametags", "Suffix", "", String.class),
   ACTION_BAR_ENABLED("Combat Tag.Display.Action Bar", "Enabled", true, Boolean.class),
   ACTION_BAR_MESSAGE("Combat Tag.Display.Action Bar", "Message", ChatUtils::colorize, String.class, String.class),
   ACTION_BAR_SYMBOL("Combat Tag.Display.Action Bar", "Symbol", "▊", String.class),
   ACTION_BAR_BARS("Combat Tag.Display.Action Bar", "Total Bars", 20, Integer.class),
   BOSS_BAR_ENABLED("Combat Tag.Display.Boss Bar", "Enabled", bool -> MCVersion.isAtLeast(MCVersion.V1_9) && bool, Boolean.class, Boolean.class),
   BOSS_BAR_MESSAGE("Combat Tag.Display.Boss Bar", "Message", ChatUtils::colorize, String.class, String.class),
   BOSS_BAR_COLOR("Combat Tag.Display.Boss Bar", "BarColor", s -> loadEnum("org.bukkit.boss.BarColor", (String)s, MCVersion.V1_9)),
   BOSS_BAR_STYLE("Combat Tag.Display.Boss Bar", "BarStyle", s -> loadEnum("org.bukkit.boss.BarStyle", (String)s, MCVersion.V1_9)),
   BLOCK_ENDERPEARL("Combat Tag.Actions Blocked", "EnderPearls", false, Boolean.class),
   BLOCK_CHORUSFRUIT("Combat Tag.Actions Blocked", "ChorusFruits", false, Boolean.class),
   BLOCK_TELEPORT("Combat Tag.Actions Blocked", "Teleport", true, Boolean.class),
   BLOCK_UNSAFE_TELEPORTS("Combat Tag.Actions Blocked", "Unsafe Teleports", false, Boolean.class),
   BLOCK_EAT("Combat Tag.Actions Blocked", "Eat", false, Boolean.class),
   BLOCK_TOTEM_UNDYING("Combat Tag.Actions Blocked", "Totem of Undying", false, Boolean.class),
   BLOCK_PLACE_BLOCKS("Combat Tag.Actions Blocked", "Place Blocks", false, Boolean.class),
   BLOCK_BREAK_BLOCKS("Combat Tag.Actions Blocked", "Break Blocks", false, Boolean.class),
   BLOCK_INVENTORY_OPEN("Combat Tag.Actions Blocked", "Open Inventory", false, Boolean.class),
   BLOCK_GLIDE_IN_COMBAT("Combat Tag.Actions Blocked", "Elytra.Block Gliding", false, Boolean.class),
   BLOCK_FIREWORKS_IN_COMBAT("Combat Tag.Actions Blocked", "Elytra.Block Fireworks", false, Boolean.class),
   FIREWORK_POWER_LIMIT("Combat Tag.Actions Blocked", "Elytra.Firework Power Limit", -1, Integer.class),
   FIREWORK_ELYTRA_COOLDOWN("Combat Tag.Actions Blocked", "Elytra.Firework Cooldown", 2, Integer.class),
   BLOCK_INTERACT_IN_COMBAT("Combat Tag.Actions Blocked", "Interact.Enabled", false, Boolean.class),
   BLOCK_INTERACT_ITEM_LIST("Combat Tag.Actions Blocked", "Interact.List", new ArrayList(), List.class),
   BLOCK_COMMANDS("Combat Tag.Actions Blocked", "Commands.Enabled", true, Boolean.class),
   BLOCK_COMMANDS_WHITELIST("Combat Tag.Actions Blocked", "Commands.Whitelist", true, Boolean.class),
   COMMANDS_ALLOWED("Combat Tag.Actions Blocked", "Commands.Command List", Conf::getList, List.class, List.class),
   FINE_AMOUNT("Combat Log Punishments", "Money Penalty", 0.0, Double.class),
   KILL_ON_LOGOUT("Combat Log Punishments", "Kill on Logout.Enabled", true, Boolean.class),
   DROP_INVENTORY("Combat Log Punishments", "Kill on Logout.Player Drops.Inventory", true, Boolean.class),
   DROP_EXP("Combat Log Punishments", "Kill on Logout.Player Drops.Experience", true, Boolean.class),
   DROP_ARMOR("Combat Log Punishments", "Kill on Logout.Player Drops.Armor", true, Boolean.class),
   PUNISH_ON_KICK("Combat Log Punishments", "Punish On Kick.Enabled", true, Boolean.class),
   MATCH_KICK_REASON("Combat Log Punishments", "Punish On Kick.Match Kick Reason", false, Boolean.class),
   PUNISH_KICK_REASONS("Combat Log Punishments", "Punish On Kick.Kick Reasons", Conf::getList, List.class, List.class),
   COMMANDS_ON_COMBATLOG("Combat Log Punishments", "Commands On Combat Log", Conf::getCommandList, List.class, List.class),
   DEFAULT_PVP("PvP Toggle", "Default PvP", true, Boolean.class),
   TOGGLE_COOLDOWN("PvP Toggle", "Cooldown", 15, Integer.class),
   TOGGLE_NAMETAG_ENABLED("PvP Toggle", "NameTags.Enabled", false, Boolean.class),
   TOGGLE_PREFIX_ON("PvP Toggle", "NameTags.Prefix On", "&1", String.class),
   TOGGLE_PREFIX_OFF("PvP Toggle", "NameTags.Prefix Off", "&2", String.class),
   PVP_DISABLED_FEE("PvP Toggle", "PvP Disabled Money Fee", 0, Integer.class),
   COMMANDS_PVP_ON("PvP Toggle", "Commands PvP On", Conf::getCommandList, List.class, List.class),
   COMMANDS_PVP_OFF("PvP Toggle", "Commands PvP Off", Conf::getCommandList, List.class, List.class),
   WORLDGUARD_OVERRIDES("PvP Toggle", "WorldGuard Overrides", true, Boolean.class),
   VULNERABLE_ENABLED("Anti Border Hopping", "Vulnerable.Enabled", true, Boolean.class),
   VULNERABLE_RENEW_TAG("Anti Border Hopping", "Vulnerable.Renew Combat Tag", true, Boolean.class),
   PUSHBACK_ENABLED("Anti Border Hopping", "Push Back.Enabled", true, Boolean.class),
   PUSHBACK_FORCE("Anti Border Hopping", "Push Back.Force", 1.2, Double.class),
   PUSHBACK_REMOVE_ELYTRA("Anti Border Hopping", "Push Back.Remove Elytra", false, Boolean.class),
   NEWBIE_ENABLED("Newbie Protection", "Enabled", true, Boolean.class),
   NEWBIE_TIME("Newbie Protection", "Time", 600, Integer.class),
   NEWBIE_ALLOW_DISABLE("Newbie Protection", "Allow Player Disable", true, Boolean.class),
   NEWBIE_BLOCK_PICK("Newbie Protection", "Block Pick Items", false, Boolean.class),
   NEWBIE_BLOCK_PLACE("Newbie Protection", "Block Place Blocks", false, Boolean.class),
   NEWBIE_BLOCK_BREAK("Newbie Protection", "Block Break Blocks", false, Boolean.class),
   NEWBIE_GODMODE("Newbie Protection", "Protect From Everything", false, Boolean.class),
   NEWBIE_BOSS_BAR_ENABLED("Newbie Protection", "Boss Bar.Enabled", bool -> MCVersion.isAtLeast(MCVersion.V1_9) && bool, Boolean.class, Boolean.class),
   NEWBIE_BOSS_BAR_MESSAGE("Newbie Protection", "Boss Bar.Message", ChatUtils::colorize, String.class, String.class),
   NEWBIE_BOSS_BAR_COLOR("Newbie Protection", "Boss Bar.BarColor", s -> loadEnum("org.bukkit.boss.BarColor", (String)s, MCVersion.V1_9)),
   NEWBIE_BOSS_BAR_STYLE("Newbie Protection", "Boss Bar.BarStyle", s -> loadEnum("org.bukkit.boss.BarStyle", (String)s, MCVersion.V1_9)),
   NEWBIE_BLACKLIST("Newbie Protection", "Command Blacklist", Conf::getList, List.class, List.class),
   ITEM_COOLDOWNS("Item Cooldowns", "", Conf::loadItemCooldowns, ConfigurationSection.class, Map.class),
   MONEY_REWARD("Player Kills", "Money Reward", 0.0, Double.class),
   MONEY_PENALTY("Player Kills", "Money Penalty", 0.0, Double.class),
   MONEY_STEAL("Player Kills", "Money Steal", false, Boolean.class),
   EXP_STEAL("Player Kills", "Exp Steal", 0.0, Double.class),
   COMMANDS_ON_KILL("Player Kills", "Commands On Kill.Commands", Conf::getCommandList, List.class, List.class),
   COMMANDS_ON_KILL_COOLDOWN("Player Kills", "Commands On Kill.Cooldown", -1, Integer.class),
   COMMANDS_ON_RESPAWN("Player Kills", "Commands On Respawn", Conf::getCommandList, List.class, List.class),
   KILLS_WG_EXCLUSIONS("Player Kills", "WorldGuard Exclusions", Conf::getSet, List.class, Set.class),
   KILL_ABUSE_ENABLED("Player Kills.Anti Kill Abuse", "Enabled", true, Boolean.class),
   KILL_ABUSE_MAX("Player Kills.Anti Kill Abuse", "Max Kills", 5, Integer.class),
   KILL_ABUSE_TIME("Player Kills.Anti Kill Abuse", "Time Limit", 20, Integer.class),
   KILL_ABUSE_WARN("Player Kills.Anti Kill Abuse", "Warn Before", true, Boolean.class),
   KILL_ABUSE_COMMANDS("Player Kills.Anti Kill Abuse", "Commands on Abuse", Conf::getCommandList, List.class, List.class),
   RESPAWN_PROTECTION("Player Kills.Anti Kill Abuse", "Respawn Protection", 3, Integer.class),
   PVP_BLOOD("Other Settings", "PvP Blood", true, Boolean.class),
   IGNORE_NO_DMG_HITS("Other Settings", "Ignore No Damage Hits", true, Boolean.class),
   PLAYER_DROP_MODE("Other Settings", "Player Drop Mode", s -> loadEnum(Conf.DropMode.class, s), String.class, Conf.DropMode.class),
   HEALTH_BELOW_NAME("Other Settings", "Show health under name.Enabled", true, Boolean.class),
   HEALTH_BELOW_NAME_SYMBOL("Other Settings", "Show health under name.Display Name", ChatUtils::colorize, String.class, String.class),
   DISABLE_FLY("Disable On Hit", "Fly", true, Boolean.class),
   RESTORE_FLY("Disable On Hit", "Restore Fly", true, Boolean.class),
   DISABLE_GAMEMODE("Disable On Hit", "GameMode", true, Boolean.class),
   DISABLE_DISGUISE("Disable On Hit", "Disguise", true, Boolean.class),
   DISABLE_GODMODE("Disable On Hit", "GodMode", true, Boolean.class),
   DISABLE_ELYTRA("Disable On Hit", "Elytra", false, Boolean.class),
   DISABLE_INVISIBILITY("Disable On Hit", "Invisibility", false, Boolean.class),
   CHECK_UPDATES("Update Check", "Enabled", true, Boolean.class),
   AUTO_UPDATE("Update Check", "Auto Update", true, Boolean.class),
   METRICS_OPT_OUT("Metrics", "Opt-out", false, Boolean.class),
   HARMFUL_POTIONS("Harmful Potions", "", Conf::getSet, List.class, Set.class),
   PLACEHOLDER_BOOLEAN_TRUE("Placeholders", "Boolean Format.true", ChatUtils::colorize, String.class, String.class),
   PLACEHOLDER_BOOLEAN_FALSE("Placeholders", "Boolean Format.false", ChatUtils::colorize, String.class, String.class),
   DEBUG_MODE("Debug Mode", "", Conf::setDebug, Boolean.class, Boolean.class);

   private final String section;
   private final String configKey;
   private final Object def;
   private Function<Object, ?> function;
   private final Class<?> clazz;
   private final Class<?> clazzResult;
   private static YamlConfiguration config;
   private static PvPManager plugin;
   private boolean boolValue;
   private String stringValue;
   private int intValue;
   private double doubleValue;
   private List<String> stringList;
   private Set<String> stringSet;
   private Enum<?> enumValue;
   private Map<ItemKey, ItemCooldown> map;

   private <T> Conf(final String section, final String configKey, final T def, final Class<T> clazz) {
      this.section = section;
      this.configKey = configKey;
      this.def = def;
      this.clazz = clazz;
      this.clazzResult = null;
   }

   private <T, R> Conf(final String section, final String configKey, final Function<T, R> function, final Class<T> clazz, final Class<R> clazzResult) {
      this.section = section;
      this.configKey = configKey;
      this.def = null;
      this.clazz = clazz;
      this.function = function;
      this.clazzResult = clazzResult;
   }

   private <R> Conf(final String section, final String configKey, final Function<Object, R> function) {
      this.section = section;
      this.configKey = configKey;
      this.def = null;
      this.clazz = null;
      this.function = function;
      this.clazzResult = null;
   }

   private void loadSetting() {
      String path;
      if (this.configKey.isEmpty()) {
         path = this.section;
      } else {
         path = this.section + "." + this.configKey;
      }

      if (this.function == null) {
         this.loadRegular(path);
      } else {
         this.loadFunction(path);
      }
   }

   private void loadRegular(@NotNull String path) {
      if (this.clazz == Boolean.class) {
         this.boolValue = config.getBoolean(path, (Boolean)this.def);
      } else if (this.clazz == String.class) {
         this.stringValue = config.getString(path, (String)this.def);
      } else if (this.clazz == Integer.class) {
         this.intValue = config.getInt(path, (Integer)this.def);
      } else if (this.clazz == Double.class) {
         this.doubleValue = config.getDouble(path, (Double)this.def);
      } else if (this.clazz == List.class) {
         this.stringList = config.getStringList(path);
      } else {
         Log.severe("Error loading config value for " + path);
      }
   }

   private void loadFunction(@NotNull String path) {
      if (this.clazzResult == Set.class) {
         this.stringSet = (Set<String>)this.function.apply(config.getStringList(path));
      } else if (this.clazzResult == List.class) {
         this.stringList = (List<String>)this.function.apply(config.getStringList(path));
      } else if (this.clazzResult == null || this.clazzResult.isEnum()) {
         this.enumValue = (Enum<?>)this.function.apply(config.getString(path));
      } else if (this.clazzResult == Map.class) {
         this.map = (Map<ItemKey, ItemCooldown>)this.function.apply(config.getConfigurationSection(path));
      } else if (this.clazzResult == String.class) {
         this.stringValue = (String)this.function.apply(config.getString(path));
      } else if (this.clazzResult == Boolean.class) {
         this.boolValue = (Boolean)this.function.apply(config.getBoolean(path));
      } else if (this.clazzResult == Integer.class) {
         this.intValue = (Integer)this.function.apply(config.getInt(path));
      } else if (this.clazzResult == Double.class) {
         this.doubleValue = (Double)this.function.apply(config.getDouble(path));
      } else {
         Log.severe("Error loading config value for " + path);
      }
   }

   public boolean asBool() {
      return this.boolValue;
   }

   @NotNull
   public String asString() {
      return this.stringValue;
   }

   public int asInt() {
      return this.intValue;
   }

   public double asDouble() {
      return this.doubleValue;
   }

   public List<String> asList() {
      return this.stringList;
   }

   public Set<String> asSet() {
      return this.stringSet;
   }

   @Nullable
   public <E extends Enum<E>> E asEnum(Class<E> en) {
      return en.cast(this.enumValue);
   }

   public Map<ItemKey, ItemCooldown> asMap() {
      return this.map;
   }

   public void set(String stringValue) {
      this.stringValue = stringValue;
   }

   public void set(boolean booleanValue) {
      this.boolValue = booleanValue;
   }

   public <E extends Enum<E>> E setEnum(Class<E> enumClass, String value) {
      E valueOf = Enum.valueOf(enumClass, value);
      this.enumValue = valueOf;
      return valueOf;
   }

   public void disable() {
      if (this.clazz == Boolean.class) {
         this.boolValue = false;
      } else if (this.clazz == List.class) {
         this.stringList.clear();
      } else if (this.clazz == Double.class) {
         this.doubleValue = 0.0;
      }
   }

   public static void initialize(PvPManager pl, YamlConfiguration yamlConfig) {
      plugin = pl;
      config = yamlConfig;
      Stream.of(values()).forEach(setting -> {
         try {
            setting.loadSetting();
         } catch (Exception var2) {
            Log.severe("Error loading settings for " + setting.section + "." + setting.configKey, var2);
         }
      });
      Log.infoColor(ChatColor.GREEN + "Using player nametags: " + ChatColor.AQUA + (NAMETAG_COMBAT_ENABLED.asBool() || TOGGLE_NAMETAG_ENABLED.asBool()));
   }

   private static <R extends Enum<R>> R loadEnum(Class<R> clazz, String value) {
      try {
         return Enum.valueOf(clazz, value);
      } catch (IllegalArgumentException var3) {
         Log.warning("Error! " + clazz.getSimpleName() + " '" + value + "' does not exist! Check your config");
         return null;
      }
   }

   private static <R extends Enum<R>> R loadEnum(String className, String value, MCVersion minVersion) {
      if (MCVersion.isAtLeast(minVersion)) {
         try {
            return loadEnum((Class<R>)Class.forName(className), value);
         } catch (ClassNotFoundException var4) {
            Log.severe(var4.getMessage(), var4);
         }
      }

      return null;
   }

   public static ConfigurationSection getPluginHooks() {
      return plugin.getConfigM().getHooksConfig().getConfigurationSection("Plugin Hooks");
   }

   private static List<String> getList(List<String> list) {
      list.removeIf(string -> string.startsWith("example"));
      return list;
   }

   private static Set<String> getSet(List<String> list) {
      return new HashSet<>(getList(list));
   }

   private static List<String> getCommandList(List<String> list) {
      Iterator<String> iterator = list.iterator();

      while (iterator.hasNext()) {
         String string = iterator.next().toLowerCase();
         if (string.startsWith("!console")) {
            string = string.substring(9);
         }

         if (string.startsWith("!player")) {
            string = string.substring(8);
         }

         if (string.isEmpty() || string.startsWith("example")) {
            iterator.remove();
         }
      }

      return list;
   }

   private static Map<ItemKey, ItemCooldown> loadItemCooldowns(ConfigurationSection section) {
      Map<ItemKey, ItemCooldown> itemCooldowns = new HashMap<>();
      if (section == null) {
         return Collections.emptyMap();
      } else {
         ConfigurationSection combat = section.getConfigurationSection("Combat");
         Map<ItemKey, Integer> combatCooldowns = readItemCooldowns(combat);
         ConfigurationSection global = section.getConfigurationSection("Global");
         Map<ItemKey, Integer> globalCooldowns = readItemCooldowns(global);
         Set<ItemKey> keys = new HashSet<>(combatCooldowns.keySet());
         keys.addAll(globalCooldowns.keySet());

         for (ItemKey key : keys) {
            itemCooldowns.put(key, new ItemCooldown(combatCooldowns.getOrDefault(key, -1), globalCooldowns.getOrDefault(key, -1)));
         }

         return itemCooldowns;
      }
   }

   private static Map<ItemKey, Integer> readItemCooldowns(ConfigurationSection cooldownSection) {
      if (cooldownSection == null) {
         return Collections.emptyMap();
      } else {
         Map<ItemKey, Integer> results = new HashMap<>();

         for (Entry<String, Object> e : cooldownSection.getValues(false).entrySet()) {
            ItemKey key = ItemKey.fromString(e.getKey());
            if (key == null) {
               Log.warning("The item " + e.getKey() + " in Item Cooldowns doesn't exist or is not valid for this MC version. Check your config");
            } else {
               results.put(key, (Integer)e.getValue());
            }
         }

         return results;
      }
   }

   public static ItemCooldown getItemCooldown(ItemKey key) {
      if (key == null) {
         return null;
      } else {
         Map<ItemKey, ItemCooldown> map = ITEM_COOLDOWNS.asMap();
         if (map == null) {
            return null;
         } else {
            ItemCooldown exact = map.get(key);
            if (exact != null) {
               return exact;
            } else {
               ItemKey wildcard = ItemKey.wildcard(key.material());
               return map.get(wildcard);
            }
         }
      }
   }

   public static boolean setDebug(Boolean debug) {
      Log.setDebug(debug);
      DEBUG_MODE.set(debug);
      return debug;
   }

   public static enum DropMode {
      ALWAYS,
      DROP,
      KEEP,
      TRANSFER,
      CLEAR;
   }
}
