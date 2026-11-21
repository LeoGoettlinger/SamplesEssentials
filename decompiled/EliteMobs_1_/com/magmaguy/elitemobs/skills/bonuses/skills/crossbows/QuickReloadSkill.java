package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
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
import org.bukkit.scheduler.BukkitRunnable;

public class QuickReloadSkill extends SkillBonus {
   public static final String SKILL_ID = "crossbows_quick_reload";
   public static final String MODIFIER_KEY_STRING = "quick_reload_speed";
   private static final int BUFF_DURATION_TICKS = 60;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, QuickReloadSkill.BuffExpiry> expiries = new ConcurrentHashMap<>();

   public QuickReloadSkill() {
      super(SkillType.CROSSBOWS, 10, "Quick Reload", "Successful hits grant a brief movement speed boost.", SkillBonusType.PASSIVE, 1, "crossbows_quick_reload");
   }

   private static void removeModifierByKey(AttributeInstance attr, NamespacedKey key) {
      for (AttributeModifier modifier : attr.getModifiers()) {
         if (modifier.getKey().equals(key)) {
            attr.removeModifier(modifier);
            return;
         }
      }
   }

   public void applyHaste(Player player) {
      if (this.isActive(player)) {
         int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
         double speedBonus = this.getSpeedBonus(skillLevel);
         AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
         if (attr != null) {
            NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "quick_reload_speed");
            QuickReloadSkill.BuffExpiry expiry = new QuickReloadSkill.BuffExpiry(player, key);
            expiry.runTaskLater(MetadataHandler.PLUGIN, 60L);

            try {
               AttributeModifier existing = attr.getModifiers().stream().filter(modifier -> modifier.getKey().equals(key)).findFirst().orElse(null);
               if (existing == null || existing.getAmount() != speedBonus) {
                  if (existing != null) {
                     attr.removeModifier(existing);
                  }

                  attr.addModifier(new AttributeModifier(key, speedBonus, Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
               }
            } catch (Error | RuntimeException var9) {
               expiry.cancel();
               throw var9;
            }

            QuickReloadSkill.BuffExpiry previous = expiries.put(player.getUniqueId(), expiry);
            if (previous != null) {
               previous.cancel();
               if (previous.player != player) {
                  previous.removeModifier();
               }
            }
         }
      }
   }

   private double getSpeedBonus(int skillLevel) {
      return scaled(0.03, 0.001, skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      QuickReloadSkill.BuffExpiry expiry = expiries.get(player.getUniqueId());
      if (expiry != null && expiry.player == player) {
         expiries.remove(player.getUniqueId(), expiry);
         expiry.cancel();
         expiry.removeModifier();
      }
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
      return this.applyLoreTemplates(
         Map.of(
            "speedBonus",
            String.format("%.0f", this.getSpeedBonus(skillLevel) * 100.0),
            "speedPercent",
            String.format("%.0f", this.getSpeedBonus(skillLevel) * 1000.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getSpeedBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "speedBonus",
            String.format("%.0f", this.getSpeedBonus(skillLevel) * 100.0),
            "speedPercent",
            String.format("%.0f", this.getSpeedBonus(skillLevel) * 1000.0)
         )
      );
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      for (QuickReloadSkill.BuffExpiry expiry : expiries.values()) {
         expiry.cancel();
         expiry.removeModifier();
      }

      expiries.clear();
      activePlayers.clear();
   }

   private static final class BuffExpiry extends BukkitRunnable {
      private final Player player;
      private final NamespacedKey key;

      private BuffExpiry(Player player, NamespacedKey key) {
         this.player = player;
         this.key = key;
      }

      public void run() {
         if (QuickReloadSkill.expiries.remove(this.player.getUniqueId(), this)) {
            this.removeModifier();
         }
      }

      private void removeModifier() {
         AttributeInstance attribute = this.player.getAttribute(Attribute.MOVEMENT_SPEED);
         if (attribute != null) {
            QuickReloadSkill.removeModifierByKey(attribute, this.key);
         }
      }
   }
}
