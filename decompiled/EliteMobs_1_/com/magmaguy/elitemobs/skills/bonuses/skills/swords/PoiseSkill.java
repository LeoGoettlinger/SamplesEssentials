package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

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

public class PoiseSkill extends SkillBonus {
   public static final String SKILL_ID = "swords_poise";
   public static final String MODIFIER_KEY_STRING = "poise_knockback_resistance";
   private static final double BASE_KNOCKBACK_REDUCTION = 0.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public PoiseSkill() {
      super(SkillType.SWORDS, 10, "Poise", "Reduces knockback taken while wielding a sword.", SkillBonusType.PASSIVE, 1, "swords_poise");
   }

   public static double getKnockbackReduction(int skillLevel) {
      return scaled(0.2, 0.003, 0.5, skillLevel);
   }

   public static boolean hasActiveSkill(UUID playerUUID) {
      return activePlayers.contains(playerUUID);
   }

   public static void applyKnockbackResistance(Player player, int skillLevel) {
      if (hasActiveSkill(player.getUniqueId())) {
         AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
         if (attr != null) {
            NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "poise_knockback_resistance");
            double reduction = getKnockbackReduction(skillLevel);

            for (AttributeModifier modifier : attr.getModifiers()) {
               if (modifier.getKey().equals(key)) {
                  if (modifier.getAmount() == reduction) {
                     return;
                  }

                  attr.removeModifier(modifier);
                  break;
               }
            }

            attr.addModifier(new AttributeModifier(key, reduction, Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
         }
      }
   }

   public static void removeKnockbackResistance(Player player) {
      AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
      if (attr != null) {
         removeModifierByKey(attr, new NamespacedKey(MetadataHandler.PLUGIN, "poise_knockback_resistance"));
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
      if (WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) == SkillType.SWORDS) {
         applyKnockbackResistance(player, skillLevel);
      } else {
         removeKnockbackResistance(player);
      }
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      removeKnockbackResistance(player);
   }

   @Override
   public void onActivate(Player player) {
      this.applyBonus(player, SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS));
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      removeKnockbackResistance(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double reduction = getKnockbackReduction(skillLevel) * 100.0;
      return this.applyLoreTemplates(Map.of("value", String.format("%.1f", reduction)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return getKnockbackReduction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getKnockbackReduction(skillLevel) * 100.0)));
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
