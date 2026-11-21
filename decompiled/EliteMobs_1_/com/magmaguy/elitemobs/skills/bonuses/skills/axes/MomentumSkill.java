package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MomentumSkill extends SkillBonus implements StackingSkill {
   public static final String SKILL_ID = "axes_momentum";
   private static final int MAX_STACKS = 8;
   private static final double DAMAGE_PER_STACK = 0.03;
   private static final int DECAY_TICKS = 40;
   private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
   private static final Map<UUID, BukkitRunnable> decayTasks = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public MomentumSkill() {
      super(SkillType.AXES, 50, "Momentum", "Build up damage with each consecutive hit.", SkillBonusType.STACKING, 3, "axes_momentum");
   }

   @Override
   public int getMaxStacks() {
      return 8;
   }

   @Override
   public int getCurrentStacks(Player player) {
      return playerStacks.getOrDefault(player.getUniqueId(), 0);
   }

   @Override
   public void addStack(Player player) {
      UUID uuid = player.getUniqueId();
      int current = playerStacks.getOrDefault(uuid, 0);
      if (current < 8) {
         playerStacks.put(uuid, current + 1);
      }

      this.resetDecayTimer(player);
   }

   @Override
   public void resetStacks(Player player) {
      playerStacks.remove(player.getUniqueId());
      this.cancelDecayTimer(player.getUniqueId());
   }

   public static double getDamageMultiplier(Player player, int skillLevel) {
      if (!activePlayers.contains(player.getUniqueId())) {
         return 1.0;
      } else {
         int stacks = playerStacks.getOrDefault(player.getUniqueId(), 0);
         double bonus = scaled(0.03, 4.0E-4, 0.08, skillLevel) * stacks;
         return 1.0 + bonus;
      }
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      return scaled(0.03, 4.0E-4, 0.08, skillLevel);
   }

   private void resetDecayTimer(final Player player) {
      final UUID uuid = player.getUniqueId();
      this.cancelDecayTimer(uuid);
      BukkitRunnable task = new BukkitRunnable() {
         public void run() {
            int current = MomentumSkill.playerStacks.getOrDefault(uuid, 0);
            if (current > 0) {
               MomentumSkill.playerStacks.put(uuid, current - 1);
               if (current - 1 > 0) {
                  MomentumSkill.this.resetDecayTimer(player);
               } else {
                  MomentumSkill.decayTasks.remove(uuid);
               }
            }
         }
      };
      task.runTaskLater(MetadataHandler.PLUGIN, 40L);
      decayTasks.put(uuid, task);
   }

   private void cancelDecayTimer(UUID uuid) {
      BukkitRunnable task = decayTasks.remove(uuid);
      if (task != null) {
         task.cancel();
      }
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      this.resetStacks(player);
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.removeBonus(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double bonusPerStack = this.getBonusPerStack(skillLevel) * 100.0;
      return this.applyLoreTemplates(
         Map.of("maxStacks", String.valueOf(8), "bonusPerStack", String.format("%.1f", bonusPerStack), "maxBonus", String.format("%.1f", bonusPerStack * 8.0))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel) * 8.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("maxBonus", String.format("%.0f", this.getBonusValue(skillLevel) * 100.0), "maxStacks", String.valueOf(8)));
   }

   @Override
   public void shutdown() {
      for (BukkitRunnable task : decayTasks.values()) {
         task.cancel();
      }

      decayTasks.clear();
      playerStacks.clear();
      activePlayers.clear();
   }
}
