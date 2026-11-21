package me.byteful.plugin.leveltools.api.trigger.impl;

import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArmorDurabilityTrigger implements Trigger {
   @NotNull
   @Override
   public String getTriggerId() {
      return "armor_durability";
   }

   @Override
   public boolean canHandle(@NotNull TriggerContext context) {
      Material armorMaterial = this.resolveArmorMaterial(context);
      if (armorMaterial == null) {
         return false;
      } else {
         TriggerProfile profile = context.getTriggerProfile();
         return profile.isSourceAllowed(armorMaterial.name());
      }
   }

   @Override
   public double calculateXpModifier(@NotNull TriggerContext context) {
      Material armorMaterial = this.resolveArmorMaterial(context);
      if (armorMaterial == null) {
         return 0.0;
      } else {
         TriggerProfile profile = context.getTriggerProfile();
         return profile.calculateXpModifier(armorMaterial.name());
      }
   }

   @Nullable
   private Material resolveArmorMaterial(@NotNull TriggerContext context) {
      Material sourceMaterial = context.getSourceAs(Material.class);
      if (sourceMaterial != null) {
         return sourceMaterial;
      } else {
         ItemStack sourceItem = context.getSourceAs(ItemStack.class);
         if (sourceItem != null) {
            return sourceItem.getType();
         } else {
            ItemStack contextItem = context.getItem();
            return contextItem.getType();
         }
      }
   }
}
