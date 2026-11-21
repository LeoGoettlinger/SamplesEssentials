package me.chancesd.pvpmanager.manager;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.PlayerData;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.tasks.CleanKillersTask;
import me.chancesd.pvpmanager.tasks.PvPToggleFeeTask;
import me.chancesd.pvpmanager.tasks.TagTask;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerManager {
   private final Map<UUID, CombatPlayer> players = new ConcurrentHashMap<>();
   private final ConfigManager configManager;
   private final DependencyManager dependencyManager;
   private final PvPManager plugin;
   private final TagTask tagTask;
   private boolean globalStatus = true;

   public PlayerManager(@NotNull PvPManager plugin) {
      this.plugin = plugin;
      this.configManager = plugin.getConfigM();
      this.dependencyManager = plugin.getDependencyManager();
      this.tagTask = new TagTask(plugin.getDisplayManager());
      Bukkit.getPluginManager().registerEvents(this.tagTask, plugin);
      if (Conf.KILL_ABUSE_ENABLED.asBool()) {
         ScheduleUtils.runAsyncTimer(new CleanKillersTask(this), Conf.KILL_ABUSE_TIME.asInt(), Conf.KILL_ABUSE_TIME.asInt(), TimeUnit.SECONDS);
      }

      if (Conf.PVP_DISABLED_FEE.asInt() != 0) {
         ScheduleUtils.runAsyncTimer(new PvPToggleFeeTask(this), 0L, 1L, TimeUnit.HOURS);
      }

      this.removeTeams();
      Bukkit.getOnlinePlayers().forEach(player -> this.createPlayer(player, true));
   }

   public final ProtectionResult checkProtection(@NotNull Player damager, @NotNull Player defender) {
      CombatPlayer attacker = this.get(damager);
      CombatPlayer attacked = this.get(defender);
      if (!Conf.DUEL_MODE_ENABLED.asBool() || !attacker.isInCombat() && !attacked.isInCombat() || attacker.isEnemyOf(attacked) && attacked.isEnemyOf(attacker)) {
         if (!attacker.hasOverride() && (!Conf.VULNERABLE_ENABLED.asBool() || !this.canAttackVulnerable(attacker, attacked))) {
            if (!this.globalStatus) {
               return new ProtectionResult(ProtectionType.GLOBAL_PROTECTION);
            } else if (!attacked.getCombatWorld().isCombatAllowed()) {
               return new ProtectionResult(ProtectionType.WORLD_PROTECTION);
            } else if (attacked.hasRespawnProtection() || attacker.hasRespawnProtection()) {
               return new ProtectionResult(ProtectionType.RESPAWN_PROTECTION, attacker.hasRespawnProtection());
            } else if (!attacked.isNewbie() && !attacker.isNewbie()) {
               if (attacker.hasPvPEnabled() && attacked.hasPvPEnabled()) {
                  return this.dependencyManager.shouldProtectAFK(defender)
                     ? new ProtectionResult(ProtectionType.AFK_PROTECTION)
                     : new ProtectionResult(ProtectionType.FAIL);
               } else {
                  return this.dependencyManager.shouldDisableProtection(damager, defender, ProtectionType.PVPDISABLED)
                     ? new ProtectionResult(ProtectionType.FAIL_PLUGIN_HOOK)
                     : new ProtectionResult(ProtectionType.PVPDISABLED, !attacker.hasPvPEnabled());
               }
            } else {
               return this.dependencyManager.shouldDisableProtection(damager, defender, ProtectionType.NEWBIE)
                  ? new ProtectionResult(ProtectionType.FAIL_PLUGIN_HOOK)
                  : new ProtectionResult(ProtectionType.NEWBIE, attacker.isNewbie());
            }
         } else {
            return new ProtectionResult(ProtectionType.FAIL_OVERRIDE);
         }
      } else {
         return new ProtectionResult(ProtectionType.DUEL_PROTECTION);
      }
   }

   public final boolean canAttack(@NotNull Player attacker, @NotNull Player defender) {
      return this.checkProtection(attacker, defender).isVulnerable();
   }

   private final boolean canAttackVulnerable(CombatPlayer attacker, CombatPlayer defender) {
      return attacker.isInCombat() && defender.isInCombat() && attacker.isEnemyOf(defender) && defender.isEnemyOf(attacker);
   }

   public final CombatPlayer get(Player player) {
      CombatPlayer combatPlayer = this.players.get(player.getUniqueId());
      if (combatPlayer != null) {
         return combatPlayer;
      } else {
         boolean save = CombatUtils.isReal(player.getUniqueId()) && !CombatUtils.isNPC(player);
         return !save ? this.createPlayer(player, save) : null;
      }
   }

   @Nullable
   public final CombatPlayer getUnchecked(Player player) {
      return this.players.get(player.getUniqueId());
   }

   @NotNull
   public final CombatPlayer createPlayer(Player player, boolean save) {
      Objects.requireNonNull(player, "Player cannot be null");
      CombatPlayer combatPlayer = new CombatPlayer(player, this.plugin);
      Log.debug("Created " + combatPlayer + " Saved: " + save);
      if (save) {
         this.players.put(combatPlayer.getUUID(), combatPlayer);
         this.loadPlayerDataAsync(combatPlayer);
      }

      return combatPlayer;
   }

   private void loadPlayerDataAsync(CombatPlayer combatPlayer) {
      ScheduleUtils.runAsync(() -> {
         try {
            Map<String, Object> userData = this.plugin.getStorageManager().getStorage().getUserData(combatPlayer);
            PlayerData data = PlayerData.fromMap(userData);
            if (combatPlayer.getPlayer().isOnline()) {
               combatPlayer.applyPlayerData(data);
               if (userData.isEmpty()) {
                  this.savePlayer(combatPlayer);
               }
            }
         } catch (Exception var4) {
            Log.severe("Failed to load player data for " + combatPlayer.getName(), var4);
         }
      });
   }

   public void savePlayer(CombatPlayer player) {
      try {
         this.plugin.getStorageManager().getStorage().saveUserData(player);
      } catch (Exception var3) {
         Log.severe("Failed to save player data for " + player.getName(), var3);
      }
   }

   public final void removePlayer(CombatPlayer player) {
      this.removePlayer(player, true);
   }

   private void removePlayer(CombatPlayer player, boolean asyncSave) {
      if (player.isLoaded()) {
         if (asyncSave) {
            ScheduleUtils.runAsync(() -> this.savePlayer(player));
         } else {
            this.savePlayer(player);
         }
      }

      player.cleanForRemoval();
      this.players.remove(player.getUUID(), player);
   }

   public final void applyPunishments(CombatPlayer player) {
      Player p = player.getPlayer();
      this.getConfigManager().getLog().logCombatLog(player);
      CombatUtils.executeCommands(Conf.COMMANDS_ON_COMBATLOG.asList(), p, player.getName());
      if (Conf.KILL_ON_LOGOUT.asBool()) {
         player.setPvpLogged(true);
         if (MCVersion.isAtLeast(MCVersion.V1_20_5)) {
            p.damage(Float.MAX_VALUE, DamageSource.builder(DamageType.GENERIC_KILL).build());
         } else {
            p.setHealth(0.0);
         }

         if (p.getHealth() > 0.0) {
            Log.warning("Failed to kill player on logout. It's possible the damage to the player was blocked by another plugin.");
            p.setHealth(0.0);
         }

         player.setPvpLogged(false);
      }

      if (Conf.FINE_AMOUNT.asDouble() != 0.0) {
         player.applyFine();
      }
   }

   public void handlePluginDisable() {
      this.tagTask.cancel();

      for (CombatPlayer p : new HashSet<>(this.players.values())) {
         this.removePlayer(p, false);
      }

      this.removeTeams();
      Log.infoColor(ChatColor.RED + "Saving player data to storage...");
   }

   private void removeTeams() {
      ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
      if (scoreboardManager != null) {
         scoreboardManager.getMainScoreboard().getTeams().forEach(team -> {
            if (team.getName().startsWith("PVP-") && team.getName().length() == 16) {
               Log.debug("Unregistered leftover team: " + team.getName() + " Entries: " + team.getEntries());
               team.unregister();
            }
         });
         Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
         Team pvpon = scoreboard.getTeam("PvPOn");
         if (pvpon != null) {
            pvpon.unregister();
         }

         Team pvpoff = scoreboard.getTeam("PvPOff");
         if (pvpoff != null) {
            pvpoff.unregister();
         }

         Objective health = scoreboard.getObjective("PvP_Health");
         if (health != null) {
            health.unregister();
         }
      }
   }

   public boolean getGlobalStatus() {
      return this.globalStatus;
   }

   public void setGlobalStatus(boolean globalStatus) {
      this.globalStatus = globalStatus;
   }

   public final Map<UUID, CombatPlayer> getPlayers() {
      return this.players;
   }

   public final Set<CombatPlayer> getPlayersInCombat() {
      return this.tagTask.getTaggedPlayers();
   }

   @NotNull
   public final PvPManager getPlugin() {
      return this.plugin;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }
}
