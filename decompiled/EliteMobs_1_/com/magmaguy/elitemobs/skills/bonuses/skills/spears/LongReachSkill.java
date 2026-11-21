package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
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

public class LongReachSkill extends SkillBonus {
   public static final String SKILL_ID = "spears_long_reach";
   public static final String MODIFIER_KEY_STRING = "long_reach_range";
   private static final double BASE_REACH_BONUS = 1.0;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public LongReachSkill() {
      super(SkillType.SPEARS, 10, "Long Reach", "Attacks have slightly increased range.", SkillBonusType.PASSIVE, 1, "spears_long_reach");
   }

   public double getReachBonus(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(1.0, 0.01, skillLevel);
   }

   public static void applyReachBonus(Player player, int skillLevel) {
      if (activePlayers.contains(player.getUniqueId())) {
         try {
            AttributeInstance attr = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
            if (attr == null) {
               return;
            }

            NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "long_reach_range");
            removeModifierByKey(attr, key);
            double bonus = SkillBonusRegistry.getSkillById("spears_long_reach") instanceof LongReachSkill lr ? lr.getReachBonus(skillLevel) : 1.0;
            attr.addModifier(new AttributeModifier(key, bonus, Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
         } catch (IllegalArgumentException | NoSuchFieldError var8) {
         }
      }
   }

   public static void removeReachBonus(Player player) {
      try {
         AttributeInstance attr = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
         if (attr == null) {
            return;
         }

         removeModifierByKey(attr, new NamespacedKey(MetadataHandler.PLUGIN, "long_reach_range"));
      } catch (IllegalArgumentException | NoSuchFieldError var2) {
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
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
      if (WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) == SkillType.SPEARS) {
         applyReachBonus(player, skillLevel);
      }
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      removeReachBonus(player);
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      removeReachBonus(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("value", String.format("%.1f", this.getReachBonus(skillLevel))));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getReachBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", this.getReachBonus(skillLevel))));
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
      activePlayers.clear();
   }
}
