package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class DefaultDropsHandler implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onDeath(EliteMobDeathEvent event) {
      if (event.getEntity() instanceof LivingEntity) {
         if (event.getEliteEntity().isVanillaLoot()) {
            if (event.getEntityDeathEvent() != null) {
               List<ItemStack> droppedItems = event.getEntityDeathEvent().getDrops();
               int mobLevel = event.getEliteEntity().getLevel();
               if (mobLevel > ItemSettingsConfig.getMaxLevelForDefaultLootMultiplier()) {
                  mobLevel = ItemSettingsConfig.getMaxLevelForDefaultLootMultiplier();
               }

               List<ItemStack> wornItems = this.inventoryItemsConstructor((LivingEntity)event.getEntity());
               if (ItemSettingsConfig.getDefaultLootMultiplier() != 0.0) {
                  for (ItemStack itemStack : droppedItems) {
                     if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                        boolean itemIsWorn = false;

                        for (ItemStack wornItem : wornItems) {
                           if (wornItem.isSimilar(itemStack)) {
                              itemIsWorn = true;
                           }
                        }

                        if (!itemIsWorn) {
                           for (int i = 0; i < mobLevel * 0.1 * ItemSettingsConfig.getDefaultLootMultiplier(); i++) {
                              event.getEntity().getLocation().getWorld().dropItem(event.getEntity().getLocation(), itemStack);
                           }
                        }
                     }
                  }
               }

               mobLevel = (int)(event.getEliteEntity().getLevel() * ItemSettingsConfig.getDefaultExperienceMultiplier());
               int droppedXP = (int)(event.getEntityDeathEvent().getDroppedExp() + event.getEntityDeathEvent().getDroppedExp() * 0.1 * mobLevel);
               event.getEntityDeathEvent().setDroppedExp(0);
               ((ExperienceOrb)event.getEntity().getWorld().spawn(event.getEntity().getLocation(), ExperienceOrb.class)).setExperience(droppedXP);
            }
         }
      }
   }

   private List<ItemStack> inventoryItemsConstructor(LivingEntity entity) {
      List<ItemStack> wornItems = new ArrayList<>();
      EntityEquipment equipment = entity.getEquipment();
      if (equipment == null) {
         return wornItems;
      } else {
         if (equipment.getItemInMainHand() != null && !equipment.getItemInMainHand().getType().equals(Material.AIR)) {
            wornItems.add(equipment.getItemInMainHand());
         }

         if (equipment.getHelmet() != null) {
            wornItems.add(equipment.getHelmet());
         }

         if (equipment.getChestplate() != null) {
            wornItems.add(equipment.getChestplate());
         }

         if (equipment.getLeggings() != null) {
            wornItems.add(equipment.getLeggings());
         }

         if (equipment.getBoots() != null) {
            wornItems.add(equipment.getBoots());
         }

         return wornItems;
      }
   }
}
