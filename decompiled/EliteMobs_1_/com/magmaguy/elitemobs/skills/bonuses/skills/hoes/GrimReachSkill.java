package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
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

public class GrimReachSkill extends SkillBonus {
   public static final String SKILL_ID = "hoes_grim_reach";
   public static final String MODIFIER_KEY_STRING = "grim_reach_range";
   private static final double BASE_DAMAGE_BONUS = 0.1;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public GrimReachSkill() {
      super(SkillType.HOES, 10, "Grim Reach", "Extended reach allows you to deal more damage.", SkillBonusType.PASSIVE, 1, "hoes_grim_reach");
   }

   public double getDamageMultiplier(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(0.1, 0.002, skillLevel);
   }

   public static double getReachBonus(int skillLevel) {
      return scaled(1.0, 0.013, 2.0, skillLevel);
   }

   public static void applyReachBonus(Player player, int skillLevel) {
      if (activePlayers.contains(player.getUniqueId())) {
         try {
            AttributeInstance attr = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
            if (attr == null) {
               return;
            }

            NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "grim_reach_range");
            removeModifierByKey(attr, key);
            double bonus = getReachBonus(skillLevel);
            attr.addModifier(new AttributeModifier(key, bonus, Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
         } catch (IllegalArgumentException | NoSuchFieldError var6) {
         }
      }
   }

   public static void removeReachBonus(Player player) {
      try {
         AttributeInstance attr = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
         if (attr == null) {
            return;
         }

         removeModifierByKey(attr, new NamespacedKey(MetadataHandler.PLUGIN, "grim_reach_range"));
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
      if (player.getInventory().getItemInMainHand().getType().name().endsWith("_HOE")) {
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
      return this.applyLoreTemplates(
         Map.of(
            "bonusPercent",
            String.format("%.1f", this.getDamageMultiplier(skillLevel) * 100.0),
            "reachBlocks",
            String.format("%.1f", getReachBonus(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageMultiplier(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "bonusPercent",
            String.format("%.1f", this.getDamageMultiplier(skillLevel) * 100.0),
            "reachBlocks",
            String.format("%.1f", getReachBonus(skillLevel))
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
