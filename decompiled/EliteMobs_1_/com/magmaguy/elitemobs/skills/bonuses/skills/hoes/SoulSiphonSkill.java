package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SoulSiphonSkill extends SkillBonus implements StackingSkill {
   public static final String SKILL_ID = "hoes_soul_siphon";
   private static final int MAX_STACKS = 10;
   private static final long STACK_DECAY_TIME = 12000L;
   private static final double BASE_BONUS_PER_STACK = 0.02;
   private static final Map<UUID, Integer> soulStacks = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> lastKillTime = new ConcurrentHashMap<>();
   private static final Map<UUID, BukkitRunnable> decayTasks = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final AtomicBoolean killListenerRegistered = new AtomicBoolean(false);
   private static SoulSiphonSkill.SoulSiphonKillListener killListener = null;

   public SoulSiphonSkill() {
      super(
         SkillType.HOES, 25, "Soul Siphon", "Each kill grants a stacking damage bonus that decays over time.", SkillBonusType.STACKING, 2, "hoes_soul_siphon"
      );
   }

   @Override
   public int getMaxStacks() {
      return 10;
   }

   @Override
   public boolean banksStacksExternally() {
      return true;
   }

   @Override
   public int getCurrentStacks(Player player) {
      this.checkAndDecayStacks(player);
      return soulStacks.getOrDefault(player.getUniqueId(), 0);
   }

   @Override
   public void addStack(Player player) {
      UUID uuid = player.getUniqueId();
      int current = this.getCurrentStacks(player);
      if (current < 10) {
         soulStacks.put(uuid, current + 1);
         player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0.0, 1.0, 0.0), 5, 0.3, 0.5, 0.3, 0.02);
      }

      lastKillTime.put(uuid, System.currentTimeMillis());
      this.scheduleDecay(player);
   }

   @Override
   public void resetStacks(Player player) {
      UUID uuid = player.getUniqueId();
      soulStacks.remove(uuid);
      lastKillTime.remove(uuid);
      BukkitRunnable task = decayTasks.remove(uuid);
      if (task != null) {
         task.cancel();
      }
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) / this.getMaxStacks() : scaled(0.02, 4.0E-4, skillLevel);
   }

   private void checkAndDecayStacks(Player player) {
      Long lastKill = lastKillTime.get(player.getUniqueId());
      if (lastKill != null && System.currentTimeMillis() - lastKill > 12000L) {
         this.resetStacks(player);
      }
   }

   private void scheduleDecay(final Player player) {
      final UUID uuid = player.getUniqueId();
      BukkitRunnable oldTask = decayTasks.remove(uuid);
      if (oldTask != null) {
         oldTask.cancel();
      }

      BukkitRunnable decayTask = new BukkitRunnable() {
         public void run() {
            SoulSiphonSkill.this.resetStacks(player);
            SoulSiphonSkill.decayTasks.remove(uuid);
            if (player.isOnline()) {
               ActionBarCompositor.show(player, ActionBarCompositor.Source.SKILL_FEEDBACK, DungeonsConfig.getSoulSiphonDecayMessage());
            }
         }
      };
      decayTask.runTaskLater(MetadataHandler.PLUGIN, 240L);
      decayTasks.put(uuid, decayTask);
   }

   public static void onKill(Player player) {
      if (activePlayers.contains(player.getUniqueId())) {
         if (SkillBonusRegistry.getSkillById("hoes_soul_siphon") instanceof SoulSiphonSkill soulSiphon) {
            soulSiphon.addStack(player);
         }
      }
   }

   private static void ensureKillListenerRegistered() {
      if (MetadataHandler.PLUGIN != null) {
         if (killListenerRegistered.compareAndSet(false, true)) {
            killListener = new SoulSiphonSkill.SoulSiphonKillListener();
            Bukkit.getPluginManager().registerEvents(killListener, MetadataHandler.PLUGIN);
         }
      }
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      ensureKillListenerRegistered();
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      this.resetStacks(player);
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      ensureKillListenerRegistered();
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.resetStacks(player);
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double bonusPerStack = this.getBonusPerStack(skillLevel) * 100.0;
      double maxBonus = bonusPerStack * 10.0;
      return this.applyLoreTemplates(
         Map.of("bonusPerStack", String.format("%.1f", bonusPerStack), "maxStacks", String.valueOf(10), "maxBonus", String.format("%.1f", maxBonus))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("bonusPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      for (BukkitRunnable task : decayTasks.values()) {
         task.cancel();
      }

      decayTasks.clear();
      soulStacks.clear();
      lastKillTime.clear();
      activePlayers.clear();
      if (killListener != null) {
         HandlerList.unregisterAll(killListener);
         killListener = null;
      }

      killListenerRegistered.set(false);
   }

   public static class SoulSiphonKillListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onEliteMobDeath(EliteMobDeathEvent event) {
         if (!SoulSiphonSkill.activePlayers.isEmpty()) {
            EntityDeathEvent entityDeathEvent = event.getEntityDeathEvent();
            if (entityDeathEvent != null && entityDeathEvent.getEntity() != null) {
               Player killer = entityDeathEvent.getEntity().getKiller();
               if (killer != null) {
                  if (WeaponIdentityResolver.progressionSkill(killer.getInventory().getItemInMainHand()) == SkillType.HOES) {
                     SoulSiphonSkill.onKill(killer);
                  }
               }
            }
         }
      }
   }
}
