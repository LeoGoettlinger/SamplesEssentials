package com.earth2me.essentials;

import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.api.IItemDb;
import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.config.ConfigurateUtil;
import com.earth2me.essentials.config.EssentialsConfiguration;
import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.libs.kyori.adventure.text.format.NamedTextColor;
import com.earth2me.essentials.signs.EssentialsSign;
import com.earth2me.essentials.signs.Signs;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.SimpleTextInput;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.ess3.provider.KnownCommandsProvider;
import net.ess3.provider.SyncCommandsProvider;
import net.essentialsx.api.v2.ChatType;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class Settings implements net.ess3.api.ISettings {
   private static final BigDecimal DEFAULT_MAX_MONEY = new BigDecimal("10000000000000");
   private static final BigDecimal DEFAULT_MIN_MONEY = new BigDecimal("-10000000000000");
   private static final String DEFAULT_PRIMARY_COLOR = NamedTextColor.GOLD.toString();
   private static final String DEFAULT_SECONDARY_COLOR = NamedTextColor.RED.toString();
   private final transient EssentialsConfiguration config;
   private final transient net.ess3.api.IEssentials ess;
   private final transient AtomicInteger reloadCount = new AtomicInteger(0);
   private final Settings.ChatFormats chatFormats = new Settings.ChatFormats();
   private int chatRadius = 0;
   private char chatShout = '!';
   private char chatQuestion = '?';
   private boolean teleportSafety;
   private boolean forceDisableTeleportSafety;
   private Set<String> disabledCommands = new HashSet<>();
   private List<String> overriddenCommands = Collections.emptyList();
   private List<String> playerCommands = Collections.emptyList();
   private final transient Map<String, Command> disabledBukkitCommands = new HashMap<>();
   private Map<String, BigDecimal> commandCosts;
   private Set<String> socialSpyCommands = new HashSet<>();
   private Set<String> muteCommands = new HashSet<>();
   private String nicknamePrefix = "~";
   private String operatorColor = null;
   private List<Material> itemSpawnBl = new ArrayList<>();
   private List<EssentialsSign> enabledSigns = new ArrayList<>();
   private boolean signsEnabled = false;
   private boolean warnOnBuildDisallow;
   private boolean debug = false;
   private boolean configDebug = false;
   private boolean economyDisabled = false;
   private BigDecimal maxMoney = DEFAULT_MAX_MONEY;
   private BigDecimal minMoney = DEFAULT_MIN_MONEY;
   private boolean economyLog = false;
   private boolean economyLogUUID = false;
   private boolean economyLogUpdate = false;
   private boolean changeDisplayName = true;
   private boolean changePlayerListName = false;
   private boolean prefixsuffixconfigured = false;
   private boolean addprefixsuffix = false;
   private boolean essentialsChatActive = false;
   private boolean disablePrefix = false;
   private boolean disableSuffix = false;
   private boolean getFreezeAfkPlayers;
   private boolean cancelAfkOnMove;
   private boolean cancelAfkOnInteract;
   private boolean sleepIgnoresAfkPlayers;
   private String afkListName;
   private boolean isAfkListName;
   private boolean broadcastAfkMessage;
   private ISettings.KeepInvPolicy vanishingItemPolicy;
   private ISettings.KeepInvPolicy bindingItemPolicy;
   private Set<String> noGodWorlds = new HashSet<>();
   private boolean registerBackInListener;
   private boolean disableItemPickupWhileAfk;
   private long teleportInvulnerabilityTime;
   private boolean teleportInvulnerability;
   private long loginAttackDelay;
   private int signUsePerSecond;
   private int mailsPerMinute;
   private long economyLagWarning;
   private long permissionsLagWarning;
   private boolean allowSilentJoin;
   private String customJoinMessage;
   private boolean isCustomJoinMessage;
   private String customQuitMessage;
   private boolean isCustomQuitMessage;
   private String customNewUsernameMessage;
   private boolean isCustomNewUsernameMessage;
   private List<String> spawnOnJoinGroups;
   private Map<Pattern, Long> commandCooldowns;
   private boolean npcsInBalanceRanking = false;
   private NumberFormat currencyFormat;
   private List<EssentialsSign> unprotectedSigns = Collections.emptyList();
   private List<String> defaultEnabledConfirmCommands;
   private ISettings.TeleportWhenFreePolicy teleportWhenFreePolicy;
   private boolean isCompassTowardsHomePerm;
   private boolean isAllowWorldInBroadcastworld;
   private String itemDbType;
   private boolean allowOldIdSigns;
   private boolean isWaterSafe;
   private boolean isSafeUsermap;
   private boolean logCommandBlockCommands;
   private boolean logConsoleCommands;
   private Set<Predicate<String>> nickBlacklist;
   private boolean resetNickOnNameChange;
   private double maxProjectileSpeed;
   private boolean removeEffectsOnHeal;
   private Map<String, String> worldAliases;
   private String primaryColor = DEFAULT_PRIMARY_COLOR;
   private String secondaryColor = DEFAULT_SECONDARY_COLOR;
   private Set<String> multiplierPerms;
   private BigDecimal defaultMultiplier;
   private List<String> afkTimeoutCommands = Collections.emptyList();
   private String currencySymbol = "$";

   public Settings(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
      this.config = new EssentialsConfiguration(new File(ess.getDataFolder(), "config.yml"), "/config.yml");
      this.reloadConfig();
   }

   @Override
   public File getConfigFile() {
      return this.config.getFile();
   }

   @Override
   public boolean getRespawnAtHome() {
      return this.config.getBoolean("respawn-at-home", false);
   }

   @Override
   public String getRandomSpawnLocation() {
      return this.config.getString("random-spawn-location", "none");
   }

   @Override
   public String getRandomRespawnLocation() {
      return this.config.getString("random-respawn-location", "none");
   }

   @Override
   public boolean isRespawnAtAnchor() {
      return this.config.getBoolean("respawn-at-anchor", false);
   }

   @Override
   public boolean getUpdateBedAtDaytime() {
      return this.config.getBoolean("update-bed-at-daytime", true);
   }

   @Override
   public Set<String> getMultipleHomes() {
      CommentedConfigurationNode section = this.config.getSection("sethome-multiple");
      return section == null ? null : ConfigurateUtil.getKeys(section);
   }

   @Override
   public int getHomeLimit(final User user) {
      int limit = 1;
      if (user.isAuthorized("essentials.sethome.multiple")) {
         limit = this.getHomeLimit("default");
      }

      Set<String> homeList = this.getMultipleHomes();
      if (homeList != null) {
         for (String set : homeList) {
            if (user.isAuthorized("essentials.sethome.multiple." + set) && limit < this.getHomeLimit(set)) {
               limit = this.getHomeLimit(set);
            }
         }
      }

      return limit;
   }

   @Override
   public int getHomeLimit(final String set) {
      return this.config.getInt("sethome-multiple." + set, this.config.getInt("sethome-multiple.default", 3));
   }

   private int _getChatRadius() {
      return this.config.getInt("chat.radius", this.config.getInt("chat-radius", 0));
   }

   @Override
   public int getChatRadius() {
      return this.chatRadius;
   }

   @Override
   public int getNearRadius() {
      return this.config.getInt("near-radius", 200);
   }

   private char _getChatShout() {
      return this.config.getString("chat.shout", "!").charAt(0);
   }

   @Override
   public char getChatShout() {
      return this.chatShout;
   }

   private char _getChatQuestion() {
      return this.config.getString("chat.question", "?").charAt(0);
   }

   @Override
   public char getChatQuestion() {
      return this.chatQuestion;
   }

   @Override
   public boolean isShoutDefault() {
      return this.config.getBoolean("chat.shout-default", false);
   }

   @Override
   public boolean isPersistShout() {
      return this.config.getBoolean("chat.persist-shout", false);
   }

   @Override
   public boolean isChatQuestionEnabled() {
      return this.config.getBoolean("chat.question-enabled", true);
   }

   @Override
   public boolean isUsePaperChatEvent() {
      return this.config.getBoolean("chat.paper-chat-events", true);
   }

   public boolean _isTeleportSafetyEnabled() {
      return this.config.getBoolean("teleport-safety", true);
   }

   @Override
   public boolean isTeleportSafetyEnabled() {
      return this.teleportSafety;
   }

   private boolean _isForceDisableTeleportSafety() {
      return this.config.getBoolean("force-disable-teleport-safety", false);
   }

   @Override
   public boolean isForceDisableTeleportSafety() {
      return this.forceDisableTeleportSafety;
   }

   @Override
   public boolean isAlwaysTeleportSafety() {
      return this.config.getBoolean("force-safe-teleport-location", false);
   }

   @Override
   public boolean isTeleportPassengerDismount() {
      return this.config.getBoolean("teleport-passenger-dismount", true);
   }

   @Override
   public boolean isForcePassengerTeleport() {
      return this.config.getBoolean("force-passenger-teleportation", false);
   }

   @Override
   public double getTeleportDelay() {
      return this.config.getDouble("teleport-delay", 0.0);
   }

   @Override
   public int getOversizedStackSize() {
      return this.config.getInt("oversized-stacksize", 64);
   }

   @Override
   public int getDefaultStackSize() {
      return this.config.getInt("default-stack-size", -1);
   }

   @Override
   public BigDecimal getStartingBalance() {
      return this.config.getBigDecimal("starting-balance", BigDecimal.ZERO);
   }

   @Override
   public boolean isCommandDisabled(final IEssentialsCommand cmd) {
      return this.isCommandDisabled(cmd.getName());
   }

   @Override
   public boolean isCommandDisabled(final String label) {
      return this.disabledCommands.contains(label);
   }

   @Override
   public Set<String> getDisabledCommands() {
      return this.disabledCommands;
   }

   @Override
   public boolean isVerboseCommandUsages() {
      return this.config.getBoolean("verbose-command-usages", true);
   }

   private void _addAlternativeCommand(final String label, final Command current) {
      KnownCommandsProvider knownCommandsProvider = this.ess.provider(KnownCommandsProvider.class);
      Command cmd = this.ess.getAlternativeCommandsHandler().getAlternative(label);
      if (cmd == null) {
         for (Entry<String, Command> entry : knownCommandsProvider.getKnownCommands().entrySet()) {
            String[] split = entry.getKey().split(":");
            if (entry.getValue() != current && split[split.length - 1].equals(label)) {
               cmd = entry.getValue();
               break;
            }
         }
      }

      if (cmd != null) {
         knownCommandsProvider.getKnownCommands().put(label, cmd);
      }
   }

   private Set<String> _getDisabledCommands() {
      Set<String> disCommands = new HashSet<>();

      for (String c : this.config.getList("disabled-commands", String.class)) {
         disCommands.add(c.toLowerCase(Locale.ENGLISH));
      }

      for (String c : this.config.getKeys()) {
         if (c.startsWith("disable-")) {
            disCommands.add(c.substring(8).toLowerCase(Locale.ENGLISH));
         }
      }

      return disCommands;
   }

   private List<String> _getPlayerCommands() {
      return this.config.getList("player-commands", String.class);
   }

   @Override
   public boolean isPlayerCommand(final String label) {
      for (String c : this.playerCommands) {
         if (c.equalsIgnoreCase(label)) {
            return true;
         }
      }

      return false;
   }

   private List<String> _getOverriddenCommands() {
      return this.config.getList("overridden-commands", String.class);
   }

   @Override
   public boolean isCommandOverridden(final String name) {
      for (String c : this.overriddenCommands) {
         if (c.equalsIgnoreCase(name)) {
            return true;
         }
      }

      return this.config.getBoolean("override-" + name.toLowerCase(Locale.ENGLISH), false);
   }

   @Override
   public BigDecimal getCommandCost(final IEssentialsCommand cmd) {
      return this.getCommandCost(cmd.getName());
   }

   private Map<String, BigDecimal> _getCommandCosts() {
      Map<String, CommentedConfigurationNode> section = ConfigurateUtil.getMap(this.config.getSection("command-costs"));
      if (!section.isEmpty()) {
         Map<String, BigDecimal> newMap = new HashMap<>();

         for (Entry<String, CommentedConfigurationNode> entry : section.entrySet()) {
            String command = entry.getKey();
            CommentedConfigurationNode node = entry.getValue();
            if (command.charAt(0) == '/') {
               this.ess.getLogger().warning("Invalid command cost. '" + command + "' should not start with '/'.");
            }

            try {
               if (ConfigurateUtil.isDouble(node)) {
                  newMap.put(command.toLowerCase(Locale.ENGLISH), BigDecimal.valueOf(node.getDouble()));
               } else if (ConfigurateUtil.isInt(node)) {
                  newMap.put(command.toLowerCase(Locale.ENGLISH), BigDecimal.valueOf((long)node.getInt()));
               } else if (ConfigurateUtil.isString(node)) {
                  String costString = node.getString();
                  double cost = Double.parseDouble(costString.trim().replace("$", "").replace(this.getCurrencySymbol(), "").replaceAll("\\W", ""));
                  newMap.put(command.toLowerCase(Locale.ENGLISH), BigDecimal.valueOf(cost));
               } else {
                  this.ess.getLogger().warning("Invalid command cost for: " + command);
               }
            } catch (Exception var10) {
               this.ess.getLogger().warning("Invalid command cost for: " + command);
            }
         }

         return newMap;
      } else {
         return null;
      }
   }

   @Override
   public BigDecimal getCommandCost(String name) {
      name = name.replace('.', '_').replace('/', '_');
      return this.commandCosts != null && this.commandCosts.containsKey(name) ? this.commandCosts.get(name) : BigDecimal.ZERO;
   }

   private Set<String> _getSocialSpyCommands() {
      Set<String> socialspyCommands = new HashSet<>();
      if (this.config.isList("socialspy-commands")) {
         for (String c : this.config.getList("socialspy-commands", String.class)) {
            socialspyCommands.add(c.toLowerCase(Locale.ENGLISH));
         }
      } else {
         socialspyCommands.addAll(
            Arrays.asList(
               "msg",
               "r",
               "mail",
               "m",
               "whisper",
               "emsg",
               "t",
               "tell",
               "er",
               "reply",
               "ereply",
               "email",
               "action",
               "describe",
               "eme",
               "eaction",
               "edescribe",
               "etell",
               "ewhisper",
               "pm"
            )
         );
      }

      return socialspyCommands;
   }

   @Override
   public Set<String> getSocialSpyCommands() {
      return this.socialSpyCommands;
   }

   @Override
   public boolean getSocialSpyListenMutedPlayers() {
      return this.config.getBoolean("socialspy-listen-muted-players", true);
   }

   @Override
   public boolean isSocialSpyMessages() {
      return this.config.getBoolean("socialspy-messages", true);
   }

   @Override
   public boolean isSocialSpyDisplayNames() {
      return this.config.getBoolean("socialspy-uses-displaynames", true);
   }

   private Set<String> _getMuteCommands() {
      Set<String> muteCommands = new HashSet<>();
      if (this.config.isList("mute-commands")) {
         for (String s : this.config.getList("mute-commands", String.class)) {
            muteCommands.add(s.toLowerCase(Locale.ENGLISH));
         }
      }

      return muteCommands;
   }

   @Override
   public Set<String> getMuteCommands() {
      return this.muteCommands;
   }

   private String _getNicknamePrefix() {
      return this.config.getString("nickname-prefix", "~");
   }

   private boolean _resetNickOnNameChange() {
      return this.config.getBoolean("reset-nick-on-name-change", false);
   }

   @Override
   public String getNicknamePrefix() {
      return this.nicknamePrefix;
   }

   @Override
   public boolean isResetNickOnNameChange() {
      return this.resetNickOnNameChange;
   }

   @Override
   public double getTeleportCooldown() {
      return this.config.getDouble("teleport-cooldown", 0.0);
   }

   @Override
   public double getHealCooldown() {
      return this.config.getDouble("heal-cooldown", 0.0);
   }

   @Override
   public CommentedConfigurationNode getKitSection() {
      return this.config.getSection("kits");
   }

   @Override
   public boolean isSkippingUsedOneTimeKitsFromKitList() {
      return this.config.getBoolean("skip-used-one-time-kits-from-kit-list", false);
   }

   @Override
   public String getOperatorColor() {
      return this.operatorColor;
   }

   private String _getOperatorColor() {
      String colorName = this.config.getString("ops-name-color", null);
      if (colorName == null) {
         return ChatColor.RED.toString();
      } else if (!colorName.equalsIgnoreCase("none") && !colorName.isEmpty()) {
         try {
            return FormatUtil.parseHexColor(colorName);
         } catch (NumberFormatException var4) {
            try {
               return ChatColor.valueOf(colorName.toUpperCase(Locale.ENGLISH)).toString();
            } catch (IllegalArgumentException var3) {
               ChatColor lastResort = ChatColor.getByChar(colorName);
               return lastResort != null ? lastResort.toString() : null;
            }
         }
      } else {
         return null;
      }
   }

   @Override
   public int getSpawnMobLimit() {
      return this.config.getInt("spawnmob-limit", 10);
   }

   @Override
   public boolean showNonEssCommandsInHelp() {
      return this.config.getBoolean("non-ess-in-help", true);
   }

   @Override
   public boolean hidePermissionlessHelp() {
      return this.config.getBoolean("hide-permissionless-help", true);
   }

   @Override
   public int getProtectCreeperMaxHeight() {
      return this.config.getInt("protect.creeper.max-height", -1);
   }

   @Override
   public boolean areSignsDisabled() {
      return !this.signsEnabled;
   }

   @Override
   public long getBackupInterval() {
      return this.config.getInt("backup.interval", 1440);
   }

   @Override
   public String getBackupCommand() {
      return this.config.getString("backup.command", null);
   }

   @Override
   public boolean isAlwaysRunBackup() {
      return this.config.getBoolean("backup.always-run", false);
   }

   @Override
   public String getChatFormat(final String group) {
      return this.getChatFormat(group, null);
   }

   @Override
   public String getChatFormat(final String group, final ChatType chatType) {
      String mFormat = this.chatFormats.getFormat(group, chatType, new Settings.ChatFormatConfigSupplier(group, chatType));
      if (this.isDebug()) {
         this.ess.getLogger().info(String.format("Found format '%s' for group '%s'", mFormat, group));
      }

      return mFormat;
   }

   @Override
   public String getWorldAlias(String world) {
      return this.worldAliases.getOrDefault(world.toLowerCase(), world);
   }

   private Map<String, String> _getWorldAliases() {
      Map<String, String> map = new HashMap<>();
      CommentedConfigurationNode section = this.config.getSection("chat.world-aliases");
      if (section == null) {
         return map;
      } else {
         for (Entry<String, CommentedConfigurationNode> entry : ConfigurateUtil.getMap(section).entrySet()) {
            map.put(entry.getKey().toLowerCase(), FormatUtil.replaceFormat(entry.getValue().getString()));
         }

         return map;
      }
   }

   @Override
   public boolean getAnnounceNewPlayers() {
      return !this.config.getString("newbies.announce-format", "-").isEmpty();
   }

   @Override
   public IText getAnnounceNewPlayerFormat() {
      return new SimpleTextInput(FormatUtil.replaceFormat(this.config.getString("newbies.announce-format", "&dWelcome {DISPLAYNAME} to the server!")));
   }

   @Override
   public String getNewPlayerKit() {
      return this.config.getString("newbies.kit", "");
   }

   @Override
   public String getNewbieSpawn() {
      return this.config.getString("newbies.spawnpoint", "default");
   }

   @Override
   public boolean getPerWarpPermission() {
      return this.config.getBoolean("per-warp-permission", false);
   }

   @Override
   public Map<String, Object> getListGroupConfig() {
      CommentedConfigurationNode node = this.config.getSection("list");
      if (node != null && node.isMap()) {
         Map<String, Object> values = ConfigurateUtil.getRawMap(node);
         if (!values.isEmpty()) {
            return values;
         }
      }

      Map<String, Object> defaultMap = new HashMap<>();
      if (this.config.getBoolean("sort-list-by-groups", false)) {
         defaultMap.put("ListByGroup", "ListByGroup");
      } else {
         defaultMap.put("Players", "*");
      }

      return defaultMap;
   }

   @Override
   public void reloadConfig() {
      this.config.load();
      this.noGodWorlds = new HashSet<>(this.config.getList("no-god-in-worlds", String.class));
      this.enabledSigns = this._getEnabledSigns();
      this.teleportSafety = this._isTeleportSafetyEnabled();
      this.forceDisableTeleportSafety = this._isForceDisableTeleportSafety();
      this.teleportInvulnerabilityTime = this._getTeleportInvulnerability();
      this.teleportInvulnerability = this._isTeleportInvulnerability();
      this.disableItemPickupWhileAfk = this._getDisableItemPickupWhileAfk();
      this.registerBackInListener = this._registerBackInListener();
      this.cancelAfkOnInteract = this._cancelAfkOnInteract();
      this.cancelAfkOnMove = this._cancelAfkOnMove();
      this.getFreezeAfkPlayers = this._getFreezeAfkPlayers();
      this.sleepIgnoresAfkPlayers = this._sleepIgnoresAfkPlayers();
      this.afkListName = this._getAfkListName();
      this.isAfkListName = this.afkListName != null && !this.afkListName.equalsIgnoreCase("none");
      this.broadcastAfkMessage = this._broadcastAfkMessage();
      this.itemSpawnBl = this._getItemSpawnBlacklist();
      this.loginAttackDelay = this._getLoginAttackDelay();
      this.signUsePerSecond = this._getSignUsePerSecond();
      this.chatFormats.clear();
      this.changeDisplayName = this._changeDisplayName();
      this.disabledCommands = this._getDisabledCommands();
      this.overriddenCommands = this._getOverriddenCommands();
      this.playerCommands = this._getPlayerCommands();
      KnownCommandsProvider knownCommandsProvider = this.ess.provider(KnownCommandsProvider.class);
      if (knownCommandsProvider != null) {
         boolean mapModified = false;
         if (!this.disabledBukkitCommands.isEmpty()) {
            if (this.isDebug()) {
               this.ess.getLogger().log(Level.INFO, "Re-adding " + this.disabledBukkitCommands.size() + " disabled commands!");
            }

            knownCommandsProvider.getKnownCommands().putAll(this.disabledBukkitCommands);
            this.disabledBukkitCommands.clear();
            mapModified = true;
         }

         if (this.reloadCount.get() < 2) {
            this.ess.scheduleSyncDelayedTask(() -> {
               for (Plugin plugin : this.ess.getServer().getPluginManager().getPlugins()) {
                  if (plugin.isEnabled()) {
                     this.ess.getAlternativeCommandsHandler().addPlugin(plugin);
                  }
               }
            });
         }

         for (String command : this.disabledCommands) {
            String effectiveAlias = command.toLowerCase(Locale.ENGLISH);
            Command toDisable = this.ess.getPluginCommand(effectiveAlias);
            if (toDisable != null) {
               if (this.isDebug()) {
                  this.ess.getLogger().log(Level.INFO, "Attempting removal of " + effectiveAlias);
               }

               Command removed = knownCommandsProvider.getKnownCommands().remove(effectiveAlias);
               if (removed != null) {
                  if (this.isDebug()) {
                     this.ess.getLogger().log(Level.INFO, "Adding command " + effectiveAlias + " to disabled map!");
                  }

                  this.disabledBukkitCommands.put(effectiveAlias, removed);
               }

               if (this.reloadCount.get() < 2) {
                  this.ess.scheduleSyncDelayedTask(() -> this._addAlternativeCommand(effectiveAlias, toDisable));
               } else {
                  this._addAlternativeCommand(effectiveAlias, toDisable);
               }

               mapModified = true;
            }
         }

         SyncCommandsProvider syncCommandsProvider = this.ess.provider(SyncCommandsProvider.class);
         if (mapModified) {
            if (this.isDebug()) {
               this.ess.getLogger().log(Level.INFO, "Syncing commands");
            }

            if (this.reloadCount.get() < 2) {
               this.ess.scheduleSyncDelayedTask(syncCommandsProvider::syncCommands);
            } else {
               syncCommandsProvider.syncCommands();
            }
         }
      }

      this.nicknamePrefix = this._getNicknamePrefix();
      this.resetNickOnNameChange = this._resetNickOnNameChange();
      this.operatorColor = this._getOperatorColor();
      this.changePlayerListName = this._changePlayerListName();
      this.configDebug = this._isDebug();
      this.prefixsuffixconfigured = this._isPrefixSuffixConfigured();
      this.addprefixsuffix = this._addPrefixSuffix();
      this.disablePrefix = this._disablePrefix();
      this.disableSuffix = this._disableSuffix();
      this.chatRadius = this._getChatRadius();
      this.chatShout = this._getChatShout();
      this.chatQuestion = this._getChatQuestion();
      this.commandCosts = this._getCommandCosts();
      this.socialSpyCommands = this._getSocialSpyCommands();
      this.warnOnBuildDisallow = this._warnOnBuildDisallow();
      this.mailsPerMinute = this._getMailsPerMinute();
      this.maxMoney = this._getMaxMoney();
      this.minMoney = this._getMinMoney();
      this.permissionsLagWarning = this._getPermissionsLagWarning();
      this.economyLagWarning = this._getEconomyLagWarning();
      this.economyLog = this._isEcoLogEnabled();
      this.economyLogUUID = this._isEcoLogUUIDEnabled();
      this.economyLogUpdate = this._isEcoLogUpdateEnabled();
      this.economyDisabled = this._isEcoDisabled();
      this.allowSilentJoin = this._allowSilentJoinQuit();
      this.customJoinMessage = this._getCustomJoinMessage();
      this.isCustomJoinMessage = !this.customJoinMessage.equals("none");
      this.customQuitMessage = this._getCustomQuitMessage();
      this.isCustomQuitMessage = !this.customQuitMessage.equals("none");
      this.customNewUsernameMessage = this._getCustomNewUsernameMessage();
      this.isCustomNewUsernameMessage = !this.customNewUsernameMessage.equals("none");
      this.muteCommands = this._getMuteCommands();
      this.spawnOnJoinGroups = this._getSpawnOnJoinGroups();
      this.commandCooldowns = this._getCommandCooldowns();
      this.npcsInBalanceRanking = this._isNpcsInBalanceRanking();
      this.currencyFormat = this._getCurrencyFormat();
      this.unprotectedSigns = this._getUnprotectedSign();
      this.defaultEnabledConfirmCommands = this._getDefaultEnabledConfirmCommands();
      this.teleportWhenFreePolicy = this._getTeleportWhenFreePolicy();
      this.isCompassTowardsHomePerm = this._isCompassTowardsHomePerm();
      this.isAllowWorldInBroadcastworld = this._isAllowWorldInBroadcastworld();
      this.itemDbType = this._getItemDbType();
      this.allowOldIdSigns = this._allowOldIdSigns();
      this.isWaterSafe = this._isWaterSafe();
      this.isSafeUsermap = this._isSafeUsermap();
      this.logCommandBlockCommands = this._logCommandBlockCommands();
      this.logConsoleCommands = this._logConsoleCommands();
      this.nickBlacklist = this._getNickBlacklist();
      this.maxProjectileSpeed = this._getMaxProjectileSpeed();
      this.removeEffectsOnHeal = this._isRemovingEffectsOnHeal();
      this.vanishingItemPolicy = this._getVanishingItemsPolicy();
      this.bindingItemPolicy = this._getBindingItemsPolicy();
      this.currencySymbol = this._getCurrencySymbol();
      this.worldAliases = this._getWorldAliases();
      this.primaryColor = this._getPrimaryColor();
      this.secondaryColor = this._getSecondaryColor();
      this.multiplierPerms = this._getMultiplierPerms();
      this.defaultMultiplier = this._getDefaultMultiplier();
      this.afkTimeoutCommands = this._getAfkTimeoutCommands();
      this.reloadCount.incrementAndGet();
   }

   void _lateLoadItemSpawnBlacklist() {
      this.itemSpawnBl = this._getItemSpawnBlacklist();
   }

   @Override
   public List<Material> itemSpawnBlacklist() {
      return this.itemSpawnBl;
   }

   private List<Material> _getItemSpawnBlacklist() {
      List<Material> epItemSpwn = new ArrayList<>();
      IItemDb itemDb = this.ess.getItemDb();
      if (itemDb != null && itemDb.isReady()) {
         for (String itemName : this.config.getString("item-spawn-blacklist", "").split(",")) {
            itemName = itemName.trim();
            if (!itemName.isEmpty()) {
               try {
                  ItemStack iStack = itemDb.get(itemName);
                  epItemSpwn.add(iStack.getType());
               } catch (Exception var8) {
                  this.ess
                     .getLogger()
                     .log(
                        Level.SEVERE,
                        this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("unknownItemInList", itemName, "item-spawn-blacklist")),
                        (Throwable)var8
                     );
               }
            }
         }

         return epItemSpwn;
      } else {
         this.ess.getLogger().log(Level.FINE, "Skipping item spawn blacklist read; item DB not yet loaded.");
         return epItemSpwn;
      }
   }

   @Override
   public List<EssentialsSign> enabledSigns() {
      return this.enabledSigns;
   }

   private List<EssentialsSign> _getEnabledSigns() {
      this.signsEnabled = false;
      List<EssentialsSign> newSigns = new ArrayList<>();

      for (String signName : this.config.getList("enabledSigns", String.class)) {
         signName = signName.trim().toUpperCase(Locale.ENGLISH);
         if (!signName.isEmpty()) {
            if (!signName.equals("COLOR") && !signName.equals("COLOUR")) {
               try {
                  newSigns.add(Signs.valueOf(signName).getSign());
               } catch (Exception var5) {
                  this.ess
                     .getLogger()
                     .log(Level.SEVERE, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("unknownItemInList", signName, "enabledSigns")));
                  continue;
               }

               this.signsEnabled = true;
            } else {
               this.signsEnabled = true;
            }
         }
      }

      return newSigns;
   }

   private boolean _warnOnBuildDisallow() {
      return this.config.getBoolean("protect.disable.warn-on-build-disallow", false);
   }

   @Override
   public boolean warnOnBuildDisallow() {
      return this.warnOnBuildDisallow;
   }

   private boolean _isDebug() {
      return this.config.getBoolean("debug", false);
   }

   @Override
   public boolean isDebug() {
      return this.debug || this.configDebug;
   }

   @Override
   public void setDebug(final boolean debug) {
      this.debug = debug;
   }

   @Override
   public boolean warnOnSmite() {
      return this.config.getBoolean("warn-on-smite", true);
   }

   @Override
   public boolean permissionBasedItemSpawn() {
      return this.config.getBoolean("permission-based-item-spawn", false);
   }

   @Override
   public String getLocale() {
      return this.config.getString("locale", "");
   }

   @Override
   public boolean isPerPlayerLocale() {
      return this.config.getBoolean("per-player-locale", false);
   }

   private String _getCurrencySymbol() {
      String value = this.config.getString("currency-symbol", "$").trim();
      if (value.length() > 1 || value.matches("\\d")) {
         value = "$";
      }

      return value;
   }

   @Override
   public String getCurrencySymbol() {
      return this.currencySymbol;
   }

   @Override
   public boolean isCurrencySymbolSuffixed() {
      return this.config.getBoolean("currency-symbol-suffix", false);
   }

   @Deprecated
   @Override
   public boolean isTradeInStacks(final int id) {
      return this.config.getBoolean("trade-in-stacks-" + id, false);
   }

   @Override
   public boolean isTradeInStacks(final Material type) {
      return this.config.getBoolean("trade-in-stacks." + type.toString().toLowerCase().replace("_", ""), false);
   }

   public boolean _isEcoDisabled() {
      return this.config.getBoolean("disable-eco", false);
   }

   @Override
   public boolean isEcoDisabled() {
      return this.economyDisabled;
   }

   @Override
   public boolean getProtectPreventSpawn(final String creatureName) {
      return this.config.getBoolean("protect.prevent.spawn." + creatureName, false);
   }

   @Override
   public List<String> getProtectListRaw(String configName) {
      List<String> list = new ArrayList<>();

      for (String itemName : this.config.getString(configName, "").split(",")) {
         itemName = itemName.trim();
         if (!itemName.isEmpty()) {
            list.add(itemName);
         }
      }

      return list;
   }

   @Override
   public List<Material> getProtectList(final String configName) {
      List<Material> list = new ArrayList<>();

      for (String itemName : this.getProtectListRaw(configName)) {
         Material mat = EnumUtil.getMaterial(itemName.toUpperCase());
         if (mat == null) {
            try {
               ItemStack itemStack = this.ess.getItemDb().get(itemName);
               mat = itemStack.getType();
            } catch (Exception var7) {
            }
         }

         if (mat == null) {
            this.ess.getLogger().log(Level.SEVERE, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("unknownItemInList", itemName, configName)));
         } else {
            list.add(mat);
         }
      }

      return list;
   }

   @Override
   public String getProtectString(final String configName) {
      return this.config.getString(configName, null);
   }

   @Override
   public boolean getProtectBoolean(final String configName, final boolean def) {
      return this.config.getBoolean(configName, def);
   }

   private BigDecimal _getMaxMoney() {
      return this.config.getBigDecimal("max-money", DEFAULT_MAX_MONEY);
   }

   @Override
   public BigDecimal getMaxMoney() {
      return this.maxMoney;
   }

   private BigDecimal _getMinMoney() {
      BigDecimal min = this.config.getBigDecimal("min-money", DEFAULT_MIN_MONEY);
      if (min.signum() > 0) {
         min = min.negate();
      }

      return min;
   }

   @Override
   public BigDecimal getMinMoney() {
      return this.minMoney;
   }

   @Override
   public boolean isEcoLogEnabled() {
      return this.economyLog;
   }

   public boolean _isEcoLogEnabled() {
      return this.config.getBoolean("economy-log-enabled", false);
   }

   public boolean _isEcoLogUUIDEnabled() {
      return this.config.getBoolean("economy-log-uuids", false);
   }

   @Override
   public boolean isEcoLogUUIDEnabled() {
      return this.economyLogUUID;
   }

   @Override
   public boolean isEcoLogUpdateEnabled() {
      return this.economyLogUpdate;
   }

   @Override
   public boolean realNamesOnList() {
      return this.config.getBoolean("real-names-on-list", false);
   }

   public boolean _isEcoLogUpdateEnabled() {
      return this.config.getBoolean("economy-log-update-enabled", false);
   }

   @Override
   public boolean removeGodOnDisconnect() {
      return this.config.getBoolean("remove-god-on-disconnect", false);
   }

   private boolean _changeDisplayName() {
      return this.config.getBoolean("change-displayname", true);
   }

   @Override
   public boolean changeDisplayName() {
      return this.changeDisplayName;
   }

   private boolean _changePlayerListName() {
      return this.config.getBoolean("change-playerlist", false);
   }

   @Override
   public boolean changePlayerListName() {
      return this.changePlayerListName;
   }

   @Override
   public boolean changeTabCompleteName() {
      return this.config.getBoolean("change-tab-complete-name", false);
   }

   @Override
   public boolean useBukkitPermissions() {
      return this.config.getBoolean("use-bukkit-permissions", false);
   }

   private boolean _addPrefixSuffix() {
      return this.config.getBoolean("add-prefix-suffix", false);
   }

   private boolean _isPrefixSuffixConfigured() {
      return this.config.hasProperty("add-prefix-suffix");
   }

   @Override
   public void setEssentialsChatActive(final boolean essentialsChatActive) {
      this.essentialsChatActive = essentialsChatActive;
   }

   @Override
   public boolean addPrefixSuffix() {
      return this.prefixsuffixconfigured ? this.addprefixsuffix : this.essentialsChatActive;
   }

   private boolean _disablePrefix() {
      return this.config.getBoolean("disablePrefix", false);
   }

   @Override
   public boolean disablePrefix() {
      return this.disablePrefix;
   }

   private boolean _disableSuffix() {
      return this.config.getBoolean("disableSuffix", false);
   }

   @Override
   public boolean disableSuffix() {
      return this.disableSuffix;
   }

   @Override
   public long getAutoAfk() {
      return this.config.getLong("auto-afk", 300L);
   }

   @Override
   public long getAutoAfkTimeout() {
      return this.config.getLong("auto-afk-timeout", this.config.getLong("auto-afk-kick", -1L));
   }

   private List<String> _getAfkTimeoutCommands() {
      return new ArrayList<>(this.config.getList("afk-timeout-commands", String.class));
   }

   @Override
   public List<String> getAfkTimeoutCommands() {
      return this.afkTimeoutCommands;
   }

   @Override
   public boolean getFreezeAfkPlayers() {
      return this.getFreezeAfkPlayers;
   }

   private boolean _getFreezeAfkPlayers() {
      return this.config.getBoolean("freeze-afk-players", false);
   }

   @Override
   public boolean cancelAfkOnMove() {
      return this.cancelAfkOnMove;
   }

   private boolean _cancelAfkOnMove() {
      return this.config.getBoolean("cancel-afk-on-move", true);
   }

   @Override
   public boolean cancelAfkOnInteract() {
      return this.cancelAfkOnInteract;
   }

   private boolean _cancelAfkOnInteract() {
      return this.config.getBoolean("cancel-afk-on-interact", true);
   }

   @Override
   public boolean cancelAfkOnChat() {
      return this.config.getBoolean("cancel-afk-on-chat", true);
   }

   @Override
   public boolean cancelAfkOnFish() {
      return this.config.getBoolean("cancel-afk-on-fish", true);
   }

   @Override
   public boolean sleepIgnoresAfkPlayers() {
      return this.sleepIgnoresAfkPlayers;
   }

   private boolean _sleepIgnoresAfkPlayers() {
      return this.config.getBoolean("sleep-ignores-afk-players", true);
   }

   @Override
   public boolean sleepIgnoresVanishedPlayers() {
      return this.config.getBoolean("sleep-ignores-vanished-player", true);
   }

   public String _getAfkListName() {
      return FormatUtil.replaceFormat(this.config.getString("afk-list-name", "none"));
   }

   @Override
   public boolean isAfkListName() {
      return this.isAfkListName;
   }

   @Override
   public String getAfkListName() {
      return this.afkListName;
   }

   @Override
   public boolean broadcastAfkMessage() {
      return this.broadcastAfkMessage;
   }

   private boolean _broadcastAfkMessage() {
      return this.config.getBoolean("broadcast-afk-message", true);
   }

   @Override
   public boolean areDeathMessagesEnabled() {
      return this.config.getBoolean("death-messages", true);
   }

   public ISettings.KeepInvPolicy _getVanishingItemsPolicy() {
      String value = this.config.getString("vanishing-items-policy", "keep").toLowerCase(Locale.ENGLISH);

      try {
         return ISettings.KeepInvPolicy.valueOf(value.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var3) {
         return ISettings.KeepInvPolicy.KEEP;
      }
   }

   @Override
   public ISettings.KeepInvPolicy getVanishingItemsPolicy() {
      return this.vanishingItemPolicy;
   }

   public ISettings.KeepInvPolicy _getBindingItemsPolicy() {
      String value = this.config.getString("binding-items-policy", "keep").toLowerCase(Locale.ENGLISH);

      try {
         return ISettings.KeepInvPolicy.valueOf(value.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var3) {
         return ISettings.KeepInvPolicy.KEEP;
      }
   }

   @Override
   public ISettings.KeepInvPolicy getBindingItemsPolicy() {
      return this.bindingItemPolicy;
   }

   @Override
   public Set<String> getNoGodWorlds() {
      return this.noGodWorlds;
   }

   @Override
   public boolean getRepairEnchanted() {
      return this.config.getBoolean("repair-enchanted", true);
   }

   @Override
   public boolean allowUnsafeEnchantments() {
      return this.config.getBoolean("unsafe-enchantments", false);
   }

   @Override
   public boolean isWorldTeleportPermissions() {
      return this.config.getBoolean("world-teleport-permissions", false);
   }

   @Override
   public boolean isWorldHomePermissions() {
      return this.config.getBoolean("world-home-permissions", false);
   }

   @Override
   public boolean registerBackInListener() {
      return this.registerBackInListener;
   }

   @Override
   public int getMaxTreeCommandRange() {
      return this.config.getInt("tree-command-range-limit", 300);
   }

   private boolean _registerBackInListener() {
      return this.config.getBoolean("register-back-in-listener", false);
   }

   @Override
   public boolean getDisableItemPickupWhileAfk() {
      return this.disableItemPickupWhileAfk;
   }

   private boolean _getDisableItemPickupWhileAfk() {
      return this.config.getBoolean("disable-item-pickup-while-afk", false);
   }

   private EventPriority getPriority(final String priority) {
      if ("none".equals(priority)) {
         return null;
      } else if ("lowest".equals(priority)) {
         return EventPriority.LOWEST;
      } else if ("low".equals(priority)) {
         return EventPriority.LOW;
      } else if ("normal".equals(priority)) {
         return EventPriority.NORMAL;
      } else if ("high".equals(priority)) {
         return EventPriority.HIGH;
      } else {
         return "highest".equals(priority) ? EventPriority.HIGHEST : EventPriority.NORMAL;
      }
   }

   @Override
   public EventPriority getRespawnPriority() {
      String priority = this.config.getString("respawn-listener-priority", "normal").toLowerCase(Locale.ENGLISH);
      return this.getPriority(priority);
   }

   @Override
   public EventPriority getSpawnJoinPriority() {
      String priority = this.config.getString("spawn-join-listener-priority", "normal").toLowerCase(Locale.ENGLISH);
      return this.getPriority(priority);
   }

   @Override
   public long getTpaAcceptCancellation() {
      return this.config.getLong("tpa-accept-cancellation", 120L);
   }

   @Override
   public int getTpaMaxRequests() {
      return this.config.getInt("tpa-max-requests", 5);
   }

   private long _getTeleportInvulnerability() {
      return this.config.getLong("teleport-invulnerability", 0L) * 1000L;
   }

   @Override
   public long getTeleportInvulnerability() {
      return this.teleportInvulnerabilityTime;
   }

   private boolean _isTeleportInvulnerability() {
      return this.config.getLong("teleport-invulnerability", 0L) > 0L;
   }

   @Override
   public boolean isTeleportInvulnerability() {
      return this.teleportInvulnerability;
   }

   private long _getLoginAttackDelay() {
      return this.config.getLong("login-attack-delay", 0L) * 1000L;
   }

   @Override
   public long getLoginAttackDelay() {
      return this.loginAttackDelay;
   }

   private int _getSignUsePerSecond() {
      int perSec = this.config.getInt("sign-use-per-second", 4);
      return perSec > 0 ? perSec : 1;
   }

   @Override
   public int getSignUsePerSecond() {
      return this.signUsePerSecond;
   }

   @Override
   public double getMaxFlySpeed() {
      double maxSpeed = this.config.getDouble("max-fly-speed", 0.8);
      return maxSpeed > 1.0 ? 1.0 : Math.abs(maxSpeed);
   }

   @Override
   public double getMaxWalkSpeed() {
      double maxSpeed = this.config.getDouble("max-walk-speed", 0.8);
      return maxSpeed > 1.0 ? 1.0 : Math.abs(maxSpeed);
   }

   private int _getMailsPerMinute() {
      return this.config.getInt("mails-per-minute", 1000);
   }

   @Override
   public int getMailsPerMinute() {
      return this.mailsPerMinute;
   }

   private long _getEconomyLagWarning() {
      return (long)(this.config.getDouble("economy-lag-warning", 25.0) * 1000000.0);
   }

   @Override
   public long getEconomyLagWarning() {
      return this.economyLagWarning;
   }

   private long _getPermissionsLagWarning() {
      return (long)(this.config.getDouble("permissions-lag-warning", 25.0) * 1000000.0);
   }

   @Override
   public long getPermissionsLagWarning() {
      return this.permissionsLagWarning;
   }

   @Override
   public long getMaxMute() {
      return this.config.getLong("max-mute-time", -1L);
   }

   @Override
   public long getMaxTempban() {
      return this.config.getLong("max-tempban-time", -1L);
   }

   @Override
   public int getMaxNickLength() {
      return this.config.getInt("max-nick-length", 30);
   }

   @Override
   public boolean ignoreColorsInMaxLength() {
      return this.config.getBoolean("ignore-colors-in-max-nick-length", false);
   }

   @Override
   public boolean hideDisplayNameInVanish() {
      return this.config.getBoolean("hide-displayname-in-vanish", false);
   }

   public boolean _allowSilentJoinQuit() {
      return this.config.getBoolean("allow-silent-join-quit", false);
   }

   @Override
   public boolean allowSilentJoinQuit() {
      return this.allowSilentJoin;
   }

   public String _getCustomJoinMessage() {
      return FormatUtil.replaceFormat(this.config.getString("custom-join-message", "none"));
   }

   @Override
   public String getCustomJoinMessage() {
      return this.customJoinMessage;
   }

   @Override
   public boolean isCustomJoinMessage() {
      return this.isCustomJoinMessage;
   }

   public String _getCustomQuitMessage() {
      return FormatUtil.replaceFormat(this.config.getString("custom-quit-message", "none"));
   }

   @Override
   public String getCustomQuitMessage() {
      return this.customQuitMessage;
   }

   @Override
   public boolean isCustomQuitMessage() {
      return this.isCustomQuitMessage;
   }

   public String _getCustomNewUsernameMessage() {
      return FormatUtil.replaceFormat(this.config.getString("custom-new-username-message", "none"));
   }

   @Override
   public String getCustomNewUsernameMessage() {
      return this.customNewUsernameMessage;
   }

   @Override
   public boolean isCustomNewUsernameMessage() {
      return this.isCustomNewUsernameMessage;
   }

   @Override
   public boolean isCustomServerFullMessage() {
      return this.config.getBoolean("use-custom-server-full-message", true);
   }

   @Override
   public boolean isCustomWhitelistMessage() {
      return this.config.getBoolean("use-custom-whitelist-message", true);
   }

   @Override
   public int getJoinQuitMessagePlayerCount() {
      return this.config.getInt("hide-join-quit-messages-above", -1);
   }

   @Override
   public boolean hasJoinQuitMessagePlayerCount() {
      return this.getJoinQuitMessagePlayerCount() >= 0;
   }

   @Override
   public boolean isNotifyNoNewMail() {
      return this.config.getBoolean("notify-no-new-mail", true);
   }

   @Override
   public boolean isDropItemsIfFull() {
      return this.config.getBoolean("drop-items-if-full", false);
   }

   @Override
   public int getMaxUserCacheCount() {
      long count = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
      return this.config.getInt("max-user-cache-count", (int)count);
   }

   @Override
   public long getMaxUserCacheValueExpiry() {
      return this.config.getLong("max-user-cache-value-expiry", 600L);
   }

   @Override
   public boolean isLastMessageReplyRecipient() {
      return this.config.getBoolean("last-message-reply-recipient", false);
   }

   @Override
   public boolean isReplyToVanished() {
      return this.config.getBoolean("last-message-reply-vanished", true);
   }

   @Override
   public BigDecimal getMinimumPayAmount() {
      return new BigDecimal(this.config.getString("minimum-pay-amount", "0.001"));
   }

   @Override
   public boolean isPayExcludesIgnoreList() {
      return this.config.getBoolean("pay-excludes-ignore-list", false);
   }

   @Override
   public long getLastMessageReplyRecipientTimeout() {
      return this.config.getLong("last-message-reply-recipient-timeout", 180L);
   }

   @Override
   public boolean isMilkBucketEasterEggEnabled() {
      return this.config.getBoolean("milk-bucket-easter-egg", true);
   }

   @Override
   public boolean isSendFlyEnableOnJoin() {
      return this.config.getBoolean("send-fly-enable-on-join", true);
   }

   @Override
   public boolean isWorldTimePermissions() {
      return this.config.getBoolean("world-time-permissions", false);
   }

   @Override
   public boolean isSpawnOnJoin() {
      return !this.spawnOnJoinGroups.isEmpty();
   }

   public List<String> _getSpawnOnJoinGroups() {
      List<String> def = Collections.emptyList();
      if (this.config.hasProperty("spawn-on-join")) {
         if (this.config.isList("spawn-on-join")) {
            return new ArrayList<>(this.config.getList("spawn-on-join", String.class));
         } else if (this.config.isBoolean("spawn-on-join")) {
            return this.config.getBoolean("spawn-on-join", true) ? Collections.singletonList("*") : def;
         } else {
            String val = this.config.get("spawn-on-join").toString();
            return !val.isEmpty() ? Collections.singletonList(val) : def;
         }
      } else {
         return def;
      }
   }

   @Override
   public List<String> getSpawnOnJoinGroups() {
      return this.spawnOnJoinGroups;
   }

   @Override
   public boolean isUserInSpawnOnJoinGroup(final IUser user) {
      for (String group : this.spawnOnJoinGroups) {
         if (group.equals("*") || user.inGroup(group)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isTeleportToCenterLocation() {
      return this.config.getBoolean("teleport-to-center", true);
   }

   private Map<Pattern, Long> _getCommandCooldowns() {
      CommentedConfigurationNode section = this.config.getSection("command-cooldowns");
      if (section == null) {
         return null;
      } else {
         Map<Pattern, Long> result = new LinkedHashMap<>();

         for (Entry<String, Object> entry : ConfigurateUtil.getRawMap(section).entrySet()) {
            String cmdEntry = entry.getKey();
            Object value = entry.getValue();
            Pattern pattern = null;
            if (cmdEntry.startsWith("^")) {
               try {
                  pattern = Pattern.compile(cmdEntry.substring(1));
               } catch (PatternSyntaxException var10) {
                  this.ess.getLogger().warning("Command cooldown error: " + var10.getMessage());
               }
            } else {
               if (cmdEntry.startsWith("\\^")) {
                  cmdEntry = cmdEntry.substring(1);
               }

               String cmd = cmdEntry.replaceAll("\\*", ".*");
               pattern = Pattern.compile(cmd + "( .*)?");
            }

            if (value instanceof String) {
               try {
                  value = Double.parseDouble(value.toString());
               } catch (NumberFormatException var11) {
               }
            }

            if (!(value instanceof Number)) {
               this.ess.getLogger().warning("Command cooldown error: '" + value + "' is not a valid cooldown");
            } else {
               double cooldown = ((Number)value).doubleValue();
               if (cooldown < 1.0) {
                  this.ess.getLogger().warning("Command cooldown with very short " + cooldown + " cooldown.");
               }

               result.put(pattern, (long)cooldown * 1000L);
            }
         }

         return result;
      }
   }

   @Override
   public boolean isCommandCooldownsEnabled() {
      return this.commandCooldowns != null;
   }

   @Override
   public long getCommandCooldownMs(final String label) {
      Entry<Pattern, Long> result = this.getCommandCooldownEntry(label);
      return result != null ? result.getValue() : -1L;
   }

   @Override
   public Entry<Pattern, Long> getCommandCooldownEntry(final String label) {
      if (this.isCommandCooldownsEnabled()) {
         for (Entry<Pattern, Long> entry : this.commandCooldowns.entrySet()) {
            boolean matches = entry.getKey().matcher(label).matches();
            if (this.isDebug()) {
               this.ess.getLogger().info(String.format("Checking command '%s' against cooldown '%s': %s", label, entry.getKey(), matches));
            }

            if (matches) {
               return entry;
            }
         }
      }

      return null;
   }

   @Override
   public boolean isCommandCooldownPersistent(final String label) {
      return this.config.getBoolean("command-cooldown-persistence", true);
   }

   private boolean _isNpcsInBalanceRanking() {
      return this.config.getBoolean("npcs-in-balance-ranking", false);
   }

   @Override
   public boolean isNpcsInBalanceRanking() {
      return this.npcsInBalanceRanking;
   }

   private NumberFormat _getCurrencyFormat() {
      String currencyFormatString = this.config.getString("currency-format", "#,##0.00");
      String symbolLocaleString = this.config.getString("currency-symbol-format-locale", null);
      DecimalFormatSymbols decimalFormatSymbols;
      if (symbolLocaleString != null) {
         decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.forLanguageTag(symbolLocaleString));
      } else {
         decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US);
      }

      DecimalFormat currencyFormat = new DecimalFormat(currencyFormatString, decimalFormatSymbols);
      currencyFormat.setRoundingMode(RoundingMode.FLOOR);
      NumberUtil.internalSetPrettyFormat(currencyFormat);
      return currencyFormat;
   }

   @Override
   public NumberFormat getCurrencyFormat() {
      return this.currencyFormat;
   }

   @Override
   public List<EssentialsSign> getUnprotectedSignNames() {
      return this.unprotectedSigns;
   }

   private List<EssentialsSign> _getUnprotectedSign() {
      List<EssentialsSign> newSigns = new ArrayList<>();

      for (String signName : this.config.getList("unprotected-sign-names", String.class)) {
         signName = signName.trim().toUpperCase(Locale.ENGLISH);
         if (!signName.isEmpty()) {
            try {
               newSigns.add(Signs.valueOf(signName).getSign());
            } catch (Exception var5) {
               this.ess
                  .getLogger()
                  .log(Level.SEVERE, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("unknownItemInList", signName, "unprotected-sign-names")));
            }
         }
      }

      return newSigns;
   }

   @Override
   public boolean isKitAutoEquip() {
      return this.config.getBoolean("kit-auto-equip", false);
   }

   @Override
   public boolean isPastebinCreateKit() {
      return this.config.getBoolean("pastebin-createkit", false);
   }

   @Override
   public boolean isUseBetterKits() {
      return this.config.getBoolean("use-nbt-serialization-in-createkit", false);
   }

   @Override
   public boolean isAllowBulkBuySell() {
      return this.config.getBoolean("allow-bulk-buy-sell", false);
   }

   @Override
   public boolean isAllowSellNamedItems() {
      return this.config.getBoolean("allow-selling-named-items", false);
   }

   @Override
   public boolean isAddingPrefixInPlayerlist() {
      return this.config.getBoolean("add-prefix-in-playerlist", false);
   }

   @Override
   public boolean isAddingSuffixInPlayerlist() {
      return this.config.getBoolean("add-suffix-in-playerlist", false);
   }

   @Override
   public int getNotifyPlayerOfMailCooldown() {
      return this.config.getInt("notify-player-of-mail-cooldown", 0);
   }

   @Override
   public int getMotdDelay() {
      return this.config.getInt("delay-motd", 0);
   }

   @Override
   public boolean isDirectHatAllowed() {
      return this.config.getBoolean("allow-direct-hat", true);
   }

   @Override
   public boolean isWorldChangeFlyResetEnabled() {
      return this.config.getBoolean("world-change-fly-reset", true);
   }

   @Override
   public boolean isWorldChangePreserveFlying() {
      return this.config.getBoolean("world-change-preserve-flying", true);
   }

   @Override
   public boolean isGamemodeChangePreserveFlying() {
      return this.config.getBoolean("gamemode-change-preserve-flying", false);
   }

   @Override
   public boolean isWorldChangeSpeedResetEnabled() {
      return this.config.getBoolean("world-change-speed-reset", true);
   }

   private List<String> _getDefaultEnabledConfirmCommands() {
      List<String> commands = this.config.getList("default-enabled-confirm-commands", String.class);
      commands.replaceAll(String::toLowerCase);
      return commands;
   }

   @Override
   public List<String> getDefaultEnabledConfirmCommands() {
      return this.defaultEnabledConfirmCommands;
   }

   @Override
   public boolean isConfirmCommandEnabledByDefault(final String commandName) {
      return this.getDefaultEnabledConfirmCommands().contains(commandName.toLowerCase());
   }

   private ISettings.TeleportWhenFreePolicy _getTeleportWhenFreePolicy() {
      if (this.config.hasProperty("teleport-back-when-freed-from-jail")) {
         return this.config.getBoolean("teleport-back-when-freed-from-jail", true)
            ? ISettings.TeleportWhenFreePolicy.BACK
            : ISettings.TeleportWhenFreePolicy.OFF;
      } else if (this.config.hasProperty("teleport-when-freed")) {
         String value = this.config.getString("teleport-when-freed", "back").replace("false", "off");

         try {
            return ISettings.TeleportWhenFreePolicy.valueOf(value.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var3) {
            throw new RuntimeException("Invalid value \"" + value + "\" for config option \"teleport-when-freed\"!", var3);
         }
      } else {
         return ISettings.TeleportWhenFreePolicy.BACK;
      }
   }

   @Override
   public ISettings.TeleportWhenFreePolicy getTeleportWhenFreePolicy() {
      return this.teleportWhenFreePolicy;
   }

   @Override
   public boolean isJailOnlineTime() {
      return this.config.getBoolean("jail-online-time", false);
   }

   private boolean _isCompassTowardsHomePerm() {
      return this.config.getBoolean("compass-towards-home-perm", false);
   }

   @Override
   public boolean isCompassTowardsHomePerm() {
      return this.isCompassTowardsHomePerm;
   }

   private boolean _isAllowWorldInBroadcastworld() {
      return this.config.getBoolean("allow-world-in-broadcastworld", false);
   }

   @Override
   public boolean isAllowWorldInBroadcastworld() {
      return this.isAllowWorldInBroadcastworld;
   }

   private String _getItemDbType() {
      return this.config.getString("item-db-type", "auto");
   }

   @Override
   public String getItemDbType() {
      return this.itemDbType;
   }

   private boolean _allowOldIdSigns() {
      return this.config.getBoolean("allow-old-id-signs", false);
   }

   @Override
   public boolean allowOldIdSigns() {
      return this.allowOldIdSigns;
   }

   private boolean _isWaterSafe() {
      boolean _isWaterSafe = this.config.getBoolean("is-water-safe", false);
      LocationUtil.setIsWaterSafe(_isWaterSafe);
      return _isWaterSafe;
   }

   @Override
   public boolean isWaterSafe() {
      return this.isWaterSafe;
   }

   private boolean _isSafeUsermap() {
      return this.config.getBoolean("safe-usermap-names", true);
   }

   @Override
   public boolean isSafeUsermap() {
      return this.isSafeUsermap;
   }

   private boolean _logCommandBlockCommands() {
      return this.config.getBoolean("log-command-block-commands", true);
   }

   @Override
   public boolean logCommandBlockCommands() {
      return this.logCommandBlockCommands;
   }

   private boolean _logConsoleCommands() {
      return this.config.getBoolean("log-console-commands", true);
   }

   @Override
   public boolean logConsoleCommands() {
      return this.logConsoleCommands;
   }

   private Set<Predicate<String>> _getNickBlacklist() {
      Set<Predicate<String>> blacklist = new HashSet<>();
      this.config.getList("nick-blacklist", String.class).forEach(entry -> {
         try {
            blacklist.add(Pattern.compile(entry).asPredicate());
         } catch (PatternSyntaxException var4) {
            this.ess.getLogger().warning("Invalid nickname blacklist regex: " + entry);
         }
      });
      return blacklist;
   }

   @Override
   public Set<Predicate<String>> getNickBlacklist() {
      return this.nickBlacklist;
   }

   private double _getMaxProjectileSpeed() {
      return this.config.getDouble("max-projectile-speed", 8.0);
   }

   @Override
   public double getMaxProjectileSpeed() {
      return this.maxProjectileSpeed;
   }

   private boolean _isRemovingEffectsOnHeal() {
      return this.config.getBoolean("remove-effects-on-heal", true);
   }

   @Override
   public boolean isRemovingEffectsOnHeal() {
      return this.removeEffectsOnHeal;
   }

   @Override
   public boolean isSpawnIfNoHome() {
      return this.config.getBoolean("spawn-if-no-home", true);
   }

   @Override
   public boolean isConfirmHomeOverwrite() {
      return this.config.getBoolean("confirm-home-overwrite", false);
   }

   @Override
   public boolean infoAfterDeath() {
      return this.config.getBoolean("send-info-after-death", false);
   }

   @Override
   public boolean isRespawnAtBed() {
      return this.config.getBoolean("respawn-at-home-bed", true);
   }

   @Override
   public boolean isUpdateCheckEnabled() {
      return this.config.getBoolean("update-check", true);
   }

   @Override
   public boolean showZeroBaltop() {
      return this.config.getBoolean("show-zero-baltop", true);
   }

   @Override
   public String getNickRegex() {
      return this.config.getString("allowed-nicks-regex", "^[a-zA-Z_0-9§]+$");
   }

   @Override
   public BigDecimal getMultiplier(final User user) {
      BigDecimal multiplier = this.defaultMultiplier;
      if (this.multiplierPerms == null) {
         return this.defaultMultiplier;
      } else {
         for (String multiplierPerm : this.multiplierPerms) {
            if (user.isAuthorized("essentials.sell.multiplier." + multiplierPerm)) {
               BigDecimal value = this.config.getBigDecimal("sell-multipliers." + multiplierPerm, BigDecimal.ZERO);
               if (value.compareTo(multiplier) > 0) {
                  multiplier = value;
               }
            }
         }

         return multiplier;
      }
   }

   private BigDecimal _getDefaultMultiplier() {
      return this.config.getBigDecimal("sell-multipliers.default", BigDecimal.ONE);
   }

   private Set<String> _getMultiplierPerms() {
      CommentedConfigurationNode section = this.config.getSection("sell-multipliers");
      return section == null ? null : ConfigurateUtil.getKeys(section);
   }

   @Override
   public int getMaxItemLore() {
      return this.config.getInt("max-itemlore-lines", 10);
   }

   @Override
   public String getPrimaryColor() {
      return this.primaryColor;
   }

   private String _getPrimaryColor() {
      String color = this.config.getString("message-colors.primary", "#ffaa00");
      String textColor = this._getTagColor(color);
      return textColor != null ? textColor : DEFAULT_PRIMARY_COLOR;
   }

   @Override
   public String getSecondaryColor() {
      return this.secondaryColor;
   }

   private String _getSecondaryColor() {
      String color = this.config.getString("message-colors.secondary", "#ff5555");
      String textColor = this._getTagColor(color);
      return textColor != null ? textColor : DEFAULT_SECONDARY_COLOR;
   }

   private String _getTagColor(final String color) {
      try {
         if (color.startsWith("#") && color.length() == 7 && NumberUtil.isHexadecimal(color.substring(1))) {
            Color.fromRGB(Integer.decode(color));
            return color;
         } else {
            return color.length() == 1
               ? Objects.requireNonNull(AdventureUtil.fromChar(color.charAt(0))).toString()
               : Objects.requireNonNull(NamedTextColor.NAMES.value(color.toLowerCase(Locale.ENGLISH))).toString();
         }
      } catch (IllegalArgumentException | NullPointerException var3) {
         return null;
      }
   }

   @Override
   public BigDecimal getBaltopMinBalance() {
      return this.config.getBigDecimal("baltop-requirements.minimum-balance", BigDecimal.ZERO);
   }

   @Override
   public int getBaltopEntryLimit() {
      return this.config.getInt("baltop-entry-limit", -1);
   }

   @Override
   public long getBaltopMinPlaytime() {
      return this.config.getLong("baltop-requirements.minimum-playtime", 0L);
   }

   private class ChatFormatConfigSupplier implements Supplier<String> {
      private final String group;
      private final ChatType chatType;

      ChatFormatConfigSupplier(String group, ChatType chatType) {
         this.group = group;
         this.chatType = chatType;
      }

      public String get() {
         String chatKey = this.chatType.key();
         String groupPath = "chat.group-formats." + (this.group == null ? "Default" : this.group);
         String configFormat = Settings.this.config.getString(groupPath + "." + chatKey, null);
         if (configFormat == null) {
            configFormat = Settings.this.config.getString(groupPath, null);
         }

         String formatPath = "chat.format";
         if (configFormat == null) {
            configFormat = Settings.this.config.getString("chat.format." + chatKey, null);
         }

         if (configFormat == null) {
            configFormat = Settings.this.config.getString("chat.format", null);
         }

         if (configFormat == null) {
            configFormat = "&7[{GROUP}]&r {DISPLAYNAME}&7:&r {MESSAGE}";
         }

         configFormat = FormatUtil.replaceFormat(configFormat);
         configFormat = configFormat.replace("{DISPLAYNAME}", "%1$s");
         configFormat = configFormat.replace("{MESSAGE}", "%2$s");
         configFormat = configFormat.replace("{GROUP}", "{0}");
         configFormat = configFormat.replace("{WORLD}", "{1}");
         configFormat = configFormat.replace("{WORLDNAME}", "{1}");
         configFormat = configFormat.replace("{SHORTWORLDNAME}", "{2}");
         configFormat = configFormat.replace("{TEAMPREFIX}", "{3}");
         configFormat = configFormat.replace("{TEAMSUFFIX}", "{4}");
         configFormat = configFormat.replace("{TEAMNAME}", "{5}");
         configFormat = configFormat.replace("{PREFIX}", "{6}");
         configFormat = configFormat.replace("{SUFFIX}", "{7}");
         configFormat = configFormat.replace("{USERNAME}", "{8}");
         configFormat = configFormat.replace("{NICKNAME}", "{9}");
         return "§r".concat(configFormat);
      }
   }

   private static class ChatFormats {
      private final Map<String, Settings.TypedChatFormat> groupFormats;
      private Settings.TypedChatFormat defaultFormat = null;

      ChatFormats() {
         this.groupFormats = new HashMap<>();
      }

      public String getFormat(String group, ChatType type, Supplier<String> configSupplier) {
         synchronized (this) {
            Settings.TypedChatFormat typedChatFormat;
            if (group == null) {
               if (this.defaultFormat == null) {
                  this.defaultFormat = new Settings.TypedChatFormat();
               }

               typedChatFormat = this.defaultFormat;
            } else {
               typedChatFormat = this.groupFormats.computeIfAbsent(group, s -> new Settings.TypedChatFormat());
            }

            return typedChatFormat.getFormat(type, configSupplier);
         }
      }

      public void clear() {
         synchronized (this) {
            this.defaultFormat = null;
            this.groupFormats.clear();
         }
      }
   }

   private static class TypedChatFormat {
      private final Map<ChatType, String> typedFormats;
      private String defaultFormat = null;

      TypedChatFormat() {
         this.typedFormats = new EnumMap<>(ChatType.class);
      }

      public String getFormat(ChatType type, Supplier<String> configSupplier) {
         String format;
         if (type == null) {
            if (this.defaultFormat == null) {
               this.defaultFormat = configSupplier.get();
            }

            format = this.defaultFormat;
         } else {
            format = this.typedFormats.computeIfAbsent(type, c -> configSupplier.get());
         }

         return format;
      }
   }
}
