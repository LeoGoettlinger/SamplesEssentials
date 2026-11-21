package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.MetadataHandler;
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
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.scheduler.BukkitRunnable;

public class FlurrySkill extends SkillBonus implements StackingSkill {
   public static final String SKILL_ID = "swords_flurry";
   public static final String MODIFIER_KEY_STRING = "flurry_attack_speed";
   private static final int MAX_STACKS = 5;
   private static final double ATTACK_SPEED_PER_STACK = 0.065;
   private static final int STACK_DECAY_TICKS = 60;
   private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
   private static final Map<UUID, BukkitRunnable> decayTasks = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public FlurrySkill() {
      super(SkillType.SWORDS, 25, "Flurry", "Consecutive hits increase your attack speed. Stacks decay over time.", SkillBonusType.STACKING, 2, "swords_flurry");
   }

   @Override
   public int getMaxStacks() {
      return 5;
   }

   @Override
   public int getCurrentStacks(Player player) {
      return playerStacks.getOrDefault(player.getUniqueId(), 0);
   }

   public static void applyAttackSpeedModifier(Player player, double speedBonus) {
      AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
      if (attr != null) {
         NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "flurry_attack_speed");
         removeModifierByKey(attr, key);
         if (speedBonus > 0.0) {
            attr.addModifier(new AttributeModifier(key, speedBonus, Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
         }
      }
   }

   public static void removeAttackSpeedModifier(Player player) {
      AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
      if (attr != null) {
         removeModifierByKey(attr, new NamespacedKey(MetadataHandler.PLUGIN, "flurry_attack_speed"));
      }
   }

   private static void removeModifierByKey(AttributeInstance attr, NamespacedKey key) {
      for (AttributeModifier modifier : attr.getModifiers()) {
         if (modifier.getKey().equals(key)) {
            attr.removeModifier(modifier);
            return;
         }
      }
   }

   @Override
   public void addStack(Player player) {
      UUID uuid = player.getUniqueId();
      int current = playerStacks.getOrDefault(uuid, 0);
      if (current < 5) {
         playerStacks.put(uuid, current + 1);
      }

      int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
      int newStacks = playerStacks.get(uuid);
      double speedBonus = newStacks * this.getBonusPerStack(skillLevel);
      applyAttackSpeedModifier(player, speedBonus);
      this.resetDecayTimer(player);
   }

   @Override
   public void resetStacks(Player player) {
      playerStacks.remove(player.getUniqueId());
      this.cancelDecayTimer(player.getUniqueId());
      removeAttackSpeedModifier(player);
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      return scaled(0.065, 0.001, skillLevel);
   }

   public static double getAttackSpeedBonus(Player player, int skillLevel) {
      if (!activePlayers.contains(player.getUniqueId())) {
         return 0.0;
      } else {
         int stacks = playerStacks.getOrDefault(player.getUniqueId(), 0);
         double bonusPerStack = scaled(0.065, 0.001, skillLevel);
         return stacks * bonusPerStack;
      }
   }

   public static boolean hasActiveSkill(UUID playerUUID) {
      return activePlayers.contains(playerUUID);
   }

   private void resetDecayTimer(final Player player) {
      final UUID uuid = player.getUniqueId();
      this.cancelDecayTimer(uuid);
      BukkitRunnable decayTask = new BukkitRunnable() {
         public void run() {
            if (FlurrySkill.decayTasks.get(uuid) == this) {
               FlurrySkill.decayTasks.remove(uuid, this);
               if (player.isOnline() && FlurrySkill.hasActiveSkill(uuid)) {
                  int current = FlurrySkill.playerStacks.getOrDefault(uuid, 0);
                  if (current > 0) {
                     FlurrySkill.playerStacks.put(uuid, current - 1);
                     double bonus = WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) == SkillType.SWORDS
                        ? (current - 1) * FlurrySkill.this.getBonusPerStack(SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS))
                        : 0.0;
                     FlurrySkill.applyAttackSpeedModifier(player, bonus);
                     if (current - 1 > 0) {
                        FlurrySkill.this.resetDecayTimer(player);
                     } else {
                        FlurrySkill.playerStacks.remove(uuid);
                     }
                  }
               } else {
                  FlurrySkill.this.resetStacks(player);
               }
            }
         }
      };
      decayTask.runTaskLater(MetadataHandler.PLUGIN, 60L);
      decayTasks.put(uuid, decayTask);
   }

   private void cancelDecayTimer(UUID uuid) {
      BukkitRunnable existing = decayTasks.remove(uuid);
      if (existing != null) {
         existing.cancel();
      }
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      UUID uuid = player.getUniqueId();
      activePlayers.remove(uuid);
      playerStacks.remove(uuid);
      this.cancelDecayTimer(uuid);
      removeAttackSpeedModifier(player);
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
         Map.of("maxStacks", String.valueOf(5), "perStack", String.format("%.1f", bonusPerStack), "maxBonus", String.format("%.1f", bonusPerStack * 5.0))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel) * 5.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("maxBonus", String.format("%.1f", this.getBonusPerStack(skillLevel) * 5.0 * 100.0), "maxStacks", String.valueOf(5))
      );
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.ATTRIBUTE_CHECK;
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
