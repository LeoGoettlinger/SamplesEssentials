package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
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

public class PolearmMasterySkill extends SkillBonus {
   public static final String SKILL_ID = "spears_polearm_mastery";
   public static final String MODIFIER_KEY_STRING = "polearm_mastery_speed";
   private static final double BASE_DAMAGE_BONUS = 0.1;
   private static final double BASE_ATTACK_SPEED_BONUS = 0.4;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public PolearmMasterySkill() {
      super(SkillType.SPEARS, 75, "Polearm Mastery", "Significant attack speed and damage increase.", SkillBonusType.PASSIVE, 4, "spears_polearm_mastery");
   }

   public double getDamageBonus(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(0.1, 0.002, skillLevel);
   }

   public static boolean hasActiveSkill(UUID playerUUID) {
      return activePlayers.contains(playerUUID);
   }

   public double getDamageMultiplier(int skillLevel) {
      return !activePlayers.isEmpty() ? 1.0 + this.getDamageBonus(skillLevel) : 1.0;
   }

   public static void applyAttackSpeedBonus(Player player, int skillLevel) {
      if (activePlayers.contains(player.getUniqueId())) {
         AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
         if (attr != null) {
            NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "polearm_mastery_speed");
            removeModifierByKey(attr, key);
            double bonus = 0.4 + skillLevel * 0.005;
            attr.addModifier(new AttributeModifier(key, bonus, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY));
         }
      }
   }

   public static void removeAttackSpeedBonus(Player player) {
      AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
      if (attr != null) {
         removeModifierByKey(attr, new NamespacedKey(MetadataHandler.PLUGIN, "polearm_mastery_speed"));
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

   public double getAttackSpeedBonus(int skillLevel) {
      return scaled(0.4, 0.005, skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
      if (WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) == SkillType.SPEARS) {
         applyAttackSpeedBonus(player, skillLevel);
      }
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      removeAttackSpeedBonus(player);
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      removeAttackSpeedBonus(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "damage",
            String.format("%.0f", this.getDamageBonus(skillLevel) * 100.0),
            "speed",
            String.format("%.0f", this.getAttackSpeedBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "damage",
            String.format("%.0f", this.getDamageBonus(skillLevel) * 100.0),
            "speed",
            String.format("%.0f", this.getAttackSpeedBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.ATTRIBUTE_CHECK;
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
