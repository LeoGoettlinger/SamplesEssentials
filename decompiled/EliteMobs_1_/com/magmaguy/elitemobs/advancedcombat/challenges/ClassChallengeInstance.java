package com.magmaguy.elitemobs.advancedcombat.challenges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassBand;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.instanced.InstancePlayerManager;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.arena.ArenaContainer;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public final class ClassChallengeInstance extends MatchInstance implements Listener {
   private final UUID runId = UUID.randomUUID();
   private final ArenaContainer container;
   private final Player challenger;
   private final ClassTrialDefinition trial;
   private final double quotedFee;
   private CustomBossEntity instructor;
   private BukkitTask combatTask;
   private BukkitTask cleanupTask;
   private boolean ending;
   private boolean closing;
   private int elapsedTicks;
   private boolean feeCharged;
   private boolean classUnlocked;

   public UUID runId() {
      return this.runId;
   }

   private ClassChallengeInstance(ArenaContainer container, Player player, ClassTrialDefinition trial, double quotedFee) {
      super(container.start(), container.exit(), 1, 1);
      this.container = container;
      this.challenger = player;
      this.trial = trial;
      this.quotedFee = quotedFee;
      this.world = container.start().getWorld();
      this.lobbyLocation = container.start();
   }

   public static double fee(ClassFormDefinition form) {
      return form.band() == ClassBand.STARTER ? 1.0 : 100.0 * Math.max(0, ItemLootShower.getCurrencyAmount(form.requiredFoundationSkillLevel()));
   }

   public static void admit(Player player, String formId, double quotedFee) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Trial admission requires the server thread");
      } else {
         AdvancedCombatModule module = AdvancedCombatModule.get();
         if (!module.canChallenge(player, formId)) {
            tell(player, "&cMeet this class's training requirements before challenging its instructor.");
         } else {
            ClassFormDefinition form = module.catalog().require(formId);
            if (Double.compare(quotedFee, fee(form)) != 0) {
               tell(player, "&eThe entry fee changed. Reopen the class menu for the current price.");
            } else {
               ArenaInstance league = ArenaInstance.getArenaInstances().get("wood_league.yml");
               if (league == null || league.isDefunct()) {
                  tell(player, "&cThe Wood League arena is not available.");
               } else if (league.getContainer().occupied()) {
                  tell(player, "&eThe arena is in use. Wait for the current group or class trial to finish.");
               } else if (EconomyHandler.checkCurrency(player.getUniqueId()) < quotedFee) {
                  tell(player, "&cThis attempt costs " + EconomyHandler.formatCurrency(quotedFee) + " coins.");
               } else {
                  ClassTrialDefinition trial;
                  try {
                     trial = ClassTrialDefinition.forForm(formId);
                  } catch (RuntimeException var10) {
                     Logger.warn("Could not prepare class trial " + formId + ": " + var10.getMessage());
                     tell(player, "&cThis instructor's trial is unavailable. Ask an administrator to check its required content. You were not charged.");
                     return;
                  }

                  ClassChallengeInstance run = new ClassChallengeInstance(league.getContainer(), player, trial, quotedFee);

                  try {
                     if (run.isDefunct() || !InstancePlayerManager.addNewPlayers(List.of(player), run, () -> module.canChallenge(player, formId))) {
                        run.destroyMatch();
                        return;
                     }

                     Bukkit.getPluginManager().registerEvents(run, MetadataHandler.PLUGIN);
                     player.closeInventory();
                     run.countdownMatch();
                  } catch (RuntimeException var11) {
                     run.destroyMatch();
                     Logger.warn("Could not admit class trial " + formId + ": " + var11.getMessage());
                     tell(player, "&cThe trial could not begin. You were not charged.");
                  }
               }
            }
         }
      }
   }

   @Override
   protected boolean isInRegion(Location location) {
      return this.container.contains(location);
   }

   @Override
   protected Location participantExitLocation(Player player) {
      return this.previousLocationOrExit(player);
   }

   @Override
   public boolean isAcceptingNewPlayers() {
      return !this.closing && super.isAcceptingNewPlayers() && this.container.availableTo(this);
   }

   @Override
   protected boolean reserveAdmission() {
      return !this.closing && this.container.acquire(this);
   }

   @Override
   protected void abortAdmission() {
      if (this.players.isEmpty()) {
         this.container.release(this);
      }
   }

   @Override
   protected boolean isAcceptingSpectator(Player player, boolean wasPlayer) {
      return false;
   }

   @Override
   protected void startMatch() {
      if (!this.closing && this.challenger.isOnline() && this.players.contains(this.challenger) && this.container.contains(this.challenger.getLocation())) {
         try {
            this.instructor = new CustomBossEntity(this.trial.boss());
            Location spawn = this.container.spawnPoint("north");
            if (spawn == null || !this.container.contains(spawn)) {
               throw new IllegalStateException("Trial arena requires an interior north spawn point");
            }

            this.instructor.spawn(spawn, true);
            if (!this.instructor.exists()) {
               throw new IllegalStateException("Instructor spawn was rejected");
            }

            this.requireActivePowers();
            if (!EconomyHandler.tryWithdraw(this.challenger.getUniqueId(), this.quotedFee)) {
               tell(this.challenger, "&cThe entry fee could not be paid. The trial was cancelled.");
               this.destroyMatch();
               return;
            }

            this.feeCharged = true;
            super.startMatch();
            if (this.instructor.getLivingEntity() instanceof Mob mob) {
               mob.setTarget(this.challenger);
            }

            this.combatTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::tickCombat, 5L, 5L);
         } catch (RuntimeException var4) {
            Logger.warn("Class trial " + this.trial.form().id() + " failed: " + var4.getMessage());
            tell(this.challenger, "&cThe instructor could not complete this trial.");
            this.refund();
            this.endMatch();
         }
      } else {
         this.destroyMatch();
      }
   }

   private void tickCombat() {
      if (!this.closing && this.state == MatchInstance.InstancedRegionState.ONGOING) {
         if (!this.instructor.exists()) {
            tell(this.challenger, "&cThe instructor disappeared; no class was unlocked.");
            this.refund();
            this.endMatch();
         } else {
            try {
               this.requireActivePowers();
            } catch (RuntimeException var2) {
               Logger.warn("Class trial combat failed for " + this.trial.form().id() + ": " + var2.getMessage());
               this.refund();
               this.endMatch();
               return;
            }

            if ((this.elapsedTicks += 5) >= 6000 && this.state == MatchInstance.InstancedRegionState.ONGOING) {
               tell(this.challenger, "&eThe trial time limit was reached.");
               this.defeat();
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void protectTrial(EliteMobDamagedByPlayerEvent event) {
      if (this.owns(event.getEliteMobEntity())) {
         if (this.state != MatchInstance.InstancedRegionState.ONGOING || !event.getPlayer().equals(this.challenger)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void protectBystanders(PlayerDamagedByEliteMobEvent event) {
      if (this.owns(event.getEliteMobEntity())) {
         if (this.state != MatchInstance.InstancedRegionState.ONGOING || !event.getPlayer().equals(this.challenger)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void instructorDefeated(EliteMobDeathEvent event) {
      if (event.getEliteEntity() == this.instructor
         && !this.closing
         && this.state == MatchInstance.InstancedRegionState.ONGOING
         && this.challenger.isOnline()
         && this.players.contains(this.challenger)) {
         if (AdvancedCombatModule.get().completeChallenge(this.challenger, this.trial.form().id())) {
            this.classUnlocked = true;
            this.victory();
         } else {
            tell(this.challenger, "&cYour class data became unavailable. Contact an administrator about this victory.");
            this.refund();
            this.endMatch();
         }
      }
   }

   @Override
   protected void endMatch() {
      if (!this.closing && !this.ending) {
         this.ending = true;
         if (this.combatTask != null) {
            this.combatTask.cancel();
         }

         super.endMatch();
         this.cleanupTask = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, this::destroyMatch);
      }
   }

   @Override
   protected void destroyMatch() {
      if (!this.closing) {
         this.closing = true;
         instances.remove(this);
         this.cancelScheduledTasks();
         if (this.combatTask != null) {
            this.combatTask.cancel();
         }

         if (this.cleanupTask != null) {
            this.cleanupTask.cancel();
         }

         HandlerList.unregisterAll(this);

         try {
            if (this.feeCharged && this.state == MatchInstance.InstancedRegionState.ONGOING) {
               this.refund();
            }

            if (this.instructor != null) {
               this.instructor.remove(RemovalReason.ARENA_RESET);
            }

            super.destroyMatch();
            this.retireInstance();
         } finally {
            this.container.release(this);
         }

         if (this.classUnlocked && this.challenger.isOnline() && AdvancedCombatModule.isInitialized()) {
            AdvancedCombatModule.get().onClassTrialEnded(this.challenger);
            tell(this.challenger, "&aUnlocked and activated " + this.trial.form().displayName() + "!");
            this.challenger
               .sendTitle(
                  ChatColorConverter.convert("&6Class Unlocked!"),
                  ChatColorConverter.convert("&f" + this.trial.form().displayName() + " &ais now active"),
                  10,
                  70,
                  20
               );
            this.challenger.playSound(this.challenger.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
         }
      }
   }

   private boolean owns(EliteEntity entity) {
      HashSet<EliteEntity> visited = new HashSet<>();

      while (entity != null && visited.add(entity)) {
         if (entity == this.instructor) {
            return true;
         }

         entity = entity.getSummoningEntity();
      }

      return false;
   }

   private void requireActivePowers() {
      List<LuaElitePower> powers = this.instructor.getElitePowers().stream().filter(LuaElitePower.class::isInstance).map(LuaElitePower.class::cast).toList();
      if (powers.isEmpty() || powers.stream().anyMatch(power -> !power.isRuntimeActive())) {
         throw new IllegalStateException("An instructor's required Lua power is unavailable");
      }
   }

   private void refund() {
      if (this.feeCharged) {
         this.feeCharged = false;
         if (EconomyHandler.refundPayment(this.challenger.getUniqueId(), this.quotedFee)) {
            tell(this.challenger, "&eYour trial entry fee was refunded.");
         } else {
            Logger.warn("Trial refund failed for " + this.challenger.getUniqueId() + ": " + this.quotedFee);
         }
      }
   }

   private static void tell(Player player, String text) {
      if (player.isOnline()) {
         player.sendMessage(ChatColorConverter.convert(text));
      }
   }
}
