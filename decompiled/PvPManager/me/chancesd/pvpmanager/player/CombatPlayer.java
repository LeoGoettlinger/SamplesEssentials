package me.chancesd.pvpmanager.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.event.NewbieProtectionEvent;
import me.chancesd.pvpmanager.event.PlayerTagEvent;
import me.chancesd.pvpmanager.event.PlayerTogglePvPEvent;
import me.chancesd.pvpmanager.event.PlayerUntagEvent;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.hook.TABHook;
import me.chancesd.pvpmanager.player.nametag.BukkitNameTag;
import me.chancesd.pvpmanager.player.nametag.NameTag;
import me.chancesd.pvpmanager.player.nametag.TABNameTag;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.sdutils.scheduler.SDTask;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.ItemKey;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.tasks.NewbieTask;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombatPlayer extends EcoPlayer {
   private boolean newbie;
   private boolean tagged;
   private boolean pvpState;
   private boolean pvpLogged;
   private boolean override;
   private boolean loaded;
   private boolean lastDeathWasPvP;
   private long toggleTime;
   private long respawnTime;
   private volatile long taggedTime;
   private volatile long totalTagTime;
   private long lastKillCommandTime;
   private NewbieTask newbieTask;
   private SDTask pendingUntagTask;
   private CombatPlayer enemy;
   private final Set<CombatPlayer> lastHitters = new HashSet<>();
   private final Map<String, Integer> victim = new HashMap<>();
   private final Map<ItemKey, Long> itemCooldown = new HashMap<>();
   private final PvPManager plugin;
   private NameTag nametag;

   public CombatPlayer(@NotNull Player player, PvPManager plugin) {
      super(player, plugin.getDependencyManager().getEconomy());
      this.plugin = plugin;
      this.pvpState = Conf.DEFAULT_PVP.asBool() || CombatUtils.isNPC(player);
      this.setCombatWorld(plugin.getWorldManager().getWorld(this.getPlayer().getWorld()));
      this.initializeNameTag();
   }

   public final long getToggleTime() {
      return this.toggleTime;
   }

   public final boolean isNewbie() {
      return this.newbie;
   }

   public final boolean isInCombat() {
      return this.tagged;
   }

   public final boolean hasPvPEnabled() {
      return this.pvpState;
   }

   public final boolean hasPvPLogged() {
      return this.pvpLogged;
   }

   public final boolean hasOverride() {
      return this.override;
   }

   @Nullable
   public CombatPlayer getEnemy() {
      return this.enemy;
   }

   public boolean isEnemyOf(CombatPlayer enemyPlayer) {
      return this.lastHitters.contains(enemyPlayer);
   }

   public void addEnemy(CombatPlayer enemyPlayer) {
      if (enemyPlayer != null && enemyPlayer != this) {
         this.enemy = enemyPlayer;
         this.lastHitters.add(enemyPlayer);
      }
   }

   public boolean removeEnemy(CombatPlayer enemyPlayer) {
      boolean success = this.lastHitters.remove(enemyPlayer);
      if (this.isInCombat() && this.getEnemies().isEmpty()) {
         this.untag(UntagReason.KILLED_ENEMY);
      }

      return success;
   }

   public Set<CombatPlayer> getEnemies() {
      return this.lastHitters;
   }

   public final void disableFly() {
      if (!this.isInCombat()) {
         this.setWasAllowedFlight(this.getPlayer().getAllowFlight());
      }

      this.getPlayer().setFlying(false);
      this.getPlayer().setAllowFlight(false);
   }

   public final void setNewbie(boolean newbie) {
      this.setNewbie(newbie, 0L);
   }

   public final void setNewbie(boolean newbie, long time) {
      if (this.newbieTask != null) {
         this.newbieTask.cancel();
         this.newbieTask = null;
      }

      if (newbie) {
         this.newbieTask = new NewbieTask(this, time);
      }

      if (this.newbie != newbie) {
         NewbieProtectionEvent event = new NewbieProtectionEvent(this.getPlayer(), this, newbie);
         ScheduleUtils.ensureMainThread(() -> Bukkit.getPluginManager().callEvent(event));
      }

      this.newbie = newbie;
   }

   public final synchronized void tag(boolean isAttacker, CombatPlayer other, long timeMiliseconds) {
      if (this.hasPerm(Permissions.EXEMPT_COMBAT_TAG)) {
         Log.debug("Not tagging " + this.getName() + " because player has permission: " + Permissions.EXEMPT_COMBAT_TAG);
      } else {
         this.taggedTime = System.currentTimeMillis();
         this.addEnemy(other);
         if (!this.tagged) {
            if (this.pendingUntagTask != null && !this.pendingUntagTask.isCancelled()) {
               this.pendingUntagTask.cancel();
               Log.debug("Cancelled stale untag task for " + this.getName());
               this.pendingUntagTask = null;
            }

            this.totalTagTime = timeMiliseconds;
            PlayerTagEvent event = new PlayerTagEvent(this.getPlayer(), this, isAttacker, other.getPlayer());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               this.totalTagTime = 0L;
            } else {
               if (Conf.GLOWING_IN_COMBAT.asBool() && MCVersion.isAtLeast(MCVersion.V1_9)) {
                  this.getPlayer().setGlowing(true);
               }

               if (Conf.CLOSE_INVENTORY_ON_TAG.asBool()) {
                  this.getPlayer().closeInventory();
               }

               if (this.nametag != null) {
                  ScheduleUtils.runAsync(this.nametag::setInCombat);
               }

               if (isAttacker) {
                  this.message(Lang.TAGGED_ATTACKER, new Object[]{other.getName()});
               } else {
                  this.message(Lang.TAGGED_DEFENDER, new Object[]{other.getName()});
               }

               this.tagged = true;
            }
         }
      }
   }

   public final void tag(boolean isAttacker, CombatPlayer other) {
      this.tag(isAttacker, other, Conf.TIME_IN_COMBAT.asInt() * 1000L);
   }

   public final synchronized void untag(UntagReason reason) {
      if (this.isInCombat()) {
         PlayerUntagEvent event = new PlayerUntagEvent(this.getPlayer(), this, reason);
         this.pendingUntagTask = ScheduleUtils.ensureMainThread(() -> {
            Bukkit.getPluginManager().callEvent(event);
            if (Conf.DISABLE_FLY.asBool() && Conf.RESTORE_FLY.asBool() && this.getWasAllowedFlight()) {
               this.getPlayer().setAllowFlight(this.getWasAllowedFlight());
            }

            this.pendingUntagTask = null;
         }, this.getPlayer());
         if (Conf.GLOWING_IN_COMBAT.asBool() && MCVersion.isAtLeast(MCVersion.V1_9)) {
            this.getPlayer().setGlowing(false);
         }

         if (this.nametag != null) {
            ScheduleUtils.runAsync(this.nametag::restoreNametag);
         }

         this.message(Lang.OUT_OF_COMBAT, new Object[0]);
         this.sendActionBar(Lang.OUT_OF_COMBAT_ACTIONBAR.msg());
         this.lastHitters.clear();
         this.tagged = false;
      }
   }

   public final void setPvP(boolean pvpState) {
      if (pvpState != this.pvpState) {
         PlayerTogglePvPEvent event = new PlayerTogglePvPEvent(this.getPlayer(), this, pvpState);
         Bukkit.getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.pvpState = pvpState;
            this.toggleTime = System.currentTimeMillis();
            if (this.nametag != null && Conf.TOGGLE_NAMETAG_ENABLED.asBool()) {
               this.nametag.setPvP(pvpState);
            }

            if (!pvpState) {
               this.message(Lang.PVPDISABLED, new Object[0]);
               CombatUtils.executeCommands(Conf.COMMANDS_PVP_OFF.asList(), this.getPlayer(), this.getName());
            } else {
               this.message(Lang.PVPENABLED, new Object[0]);
               CombatUtils.executeCommands(Conf.COMMANDS_PVP_ON.asList(), this.getPlayer(), this.getName());
            }
         }
      }
   }

   public final void addVictim(Player victimPlayer) {
      String victimName = victimPlayer.getName();
      if (!this.victim.containsKey(victimName)) {
         this.victim.put(victimName, 1);
      } else {
         int totalKills = this.victim.get(victimName);
         if (totalKills < Conf.KILL_ABUSE_MAX.asInt()) {
            this.victim.put(victimName, ++totalKills);
         }

         if (Conf.KILL_ABUSE_WARN.asBool() && totalKills + 1 == Conf.KILL_ABUSE_MAX.asInt()) {
            this.message(Lang.KILL_ABUSE_WARNING, new Object[0]);
         }

         if (totalKills >= Conf.KILL_ABUSE_MAX.asInt()) {
            this.untag(UntagReason.KICKED);
            CombatUtils.executeCommands(Conf.KILL_ABUSE_COMMANDS.asList(), this.getPlayer(), this.getName(), victimName);
         }
      }
   }

   public final int getKillAbuseCount(Player victimPlayer) {
      return this.victim.getOrDefault(victimPlayer.getName(), 0);
   }

   public final boolean hasItemCooldown(ItemKey key) {
      Long time = this.itemCooldown.get(key);
      if (time == null) {
         return false;
      } else if (System.currentTimeMillis() > time) {
         this.itemCooldown.remove(key);
         return false;
      } else {
         return true;
      }
   }

   public final void setItemCooldown(@NotNull ItemKey key, ItemCooldown cooldown) {
      int time = this.isInCombat() ? cooldown.getCombatCooldown() : cooldown.getGlobalCooldown();
      if (time >= 0) {
         this.itemCooldown.put(key, System.currentTimeMillis() + time * 1000);
         if (MCVersion.isAtLeast(MCVersion.V1_11_2)) {
            this.getPlayer().setCooldown(key.material(), time * 20);
         }
      }
   }

   public final void resetItemCooldowns() {
      this.itemCooldown.clear();
   }

   public final Long getItemCooldown(ItemKey key) {
      return this.itemCooldown.getOrDefault(key, 0L);
   }

   public final void clearVictims() {
      this.victim.clear();
   }

   public final void setPvpLogged(boolean pvpLogged) {
      this.pvpLogged = pvpLogged;
   }

   public final boolean hasRespawnProtection() {
      if (this.respawnTime == 0L) {
         return false;
      } else if (CombatUtils.hasTimePassed(this.respawnTime, Conf.RESPAWN_PROTECTION.asInt())) {
         this.respawnTime = 0L;
         return false;
      } else {
         return !this.hasPerm(Permissions.EXEMPT_PROTECTION_RESPAWN);
      }
   }

   public final void setRespawnTime(long respawnTime) {
      this.respawnTime = respawnTime;
   }

   public final boolean toggleOverride() {
      this.override = !this.override;
      return this.override;
   }

   public final long getTaggedTime() {
      return this.taggedTime;
   }

   public final long getUntagTime() {
      return this.taggedTime + this.totalTagTime;
   }

   public final long getTotalTagTime() {
      return this.totalTagTime;
   }

   public NewbieTask getNewbieTask() {
      return this.newbieTask;
   }

   public final long getNewbieTimeLeft() {
      return this.newbieTask != null ? this.newbieTask.getTimeleft() : 0L;
   }

   public long getTagTimeLeft() {
      return !this.tagged ? 0L : Math.max(this.getUntagTime() - System.currentTimeMillis(), 0L);
   }

   public boolean canExecuteKillCommand() {
      int cooldown = Conf.COMMANDS_ON_KILL_COOLDOWN.asInt();
      if (cooldown == -1) {
         return true;
      } else if (!CombatUtils.hasTimePassed(this.lastKillCommandTime, cooldown)) {
         return false;
      } else {
         this.lastKillCommandTime = System.currentTimeMillis();
         return true;
      }
   }

   public boolean wasLastDeathPvP() {
      return this.lastDeathWasPvP;
   }

   public void setLastDeathWasPvP(boolean lastDeathWasPvP) {
      this.lastDeathWasPvP = lastDeathWasPvP;
   }

   private void initializeNameTag() {
      if (Conf.NAMETAG_COMBAT_ENABLED.asBool() || Conf.TOGGLE_NAMETAG_ENABLED.asBool()) {
         try {
            TABHook tab = (TABHook)this.plugin.getDependencyManager().getDependency(Hook.TAB);
            if (tab == null || !tab.showAboveHead() && !tab.showInPlayerlist()) {
               if (Conf.USE_TEAMS.asBool()) {
                  this.nametag = new BukkitNameTag(this);
               }
            } else {
               this.nametag = new TABNameTag(tab, this);
            }
         } catch (NoSuchMethodError var2) {
            Conf.NAMETAG_COMBAT_ENABLED.disable();
            Conf.TOGGLE_NAMETAG_ENABLED.disable();
            this.nametag = null;
            Log.warning("Colored nametags disabled. You need to update your Spigot version.");
         } catch (IllegalStateException | UnsupportedOperationException var3) {
            Conf.NAMETAG_COMBAT_ENABLED.disable();
            Conf.TOGGLE_NAMETAG_ENABLED.disable();
            this.nametag = null;
            Log.infoColor(ChatColor.RED + "Nametag support disabled until Folia supports the scoreboard API, or use the TAB plugin");
         }
      }
   }

   public synchronized void applyPlayerData(PlayerData data) {
      this.pvpState = data.isPvpEnabled();
      this.toggleTime = data.getToggleTime();
      this.newbie = data.isNewbie();
      if (!Conf.NEWBIE_ENABLED.asBool() || (!this.newbie || data.getNewbieTimeLeft() <= 0L) && (!data.isDefault() || this.getPlayer().hasPlayedBefore())) {
         if (!Conf.NEWBIE_ENABLED.asBool() && this.newbie) {
            this.setNewbie(false);
         }
      } else {
         this.setNewbie(true, data.getNewbieTimeLeft());
      }

      if (this.getCombatWorld().isPvPForced() != CombatWorld.WorldOptionState.NONE) {
         this.pvpState = this.getCombatWorld().isPvPForced() == CombatWorld.WorldOptionState.ON;
      }

      this.loaded = true;
      this.notifyAll();
      Log.debug("Finished loading data for " + this + (this.nametag != null ? " with " + this.nametag.getClass().getSimpleName() : ""));
   }

   public PlayerData exportPlayerData() {
      return PlayerData.builder()
         .uuid(this.getUUID())
         .name(this.getName())
         .displayName(this.getPlayer().getDisplayName())
         .pvpEnabled(this.hasPvPEnabled())
         .toggleTime(this.getToggleTime())
         .newbie(this.isNewbie())
         .newbieTimeLeft(this.getNewbieTimeLeft())
         .lastSeen(System.currentTimeMillis())
         .build(false);
   }

   public final void cleanForRemoval() {
      if (this.newbieTask != null) {
         this.newbieTask.cancel();
      }

      if (this.nametag != null) {
         this.nametag.cleanup();
      }
   }

   @Nullable
   public NameTag getNameTag() {
      return this.nametag;
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public synchronized void waitForPlayerToLoad() {
      try {
         while (!this.isLoaded()) {
            Log.debug("Waiting on data loading for " + this);
            this.wait(100L);
         }
      } catch (InterruptedException var2) {
         Log.severe(var2.getMessage(), var2);
         Thread.currentThread().interrupt();
      }
   }

   @Nullable
   public static CombatPlayer get(Player player) {
      return PvPManager.getInstance().getPlayerManager().get(player);
   }

   @Override
   public boolean equals(Object obj) {
      return super.equals(obj);
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }
}
