package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.ItemDurability;
import com.magmaguy.elitemobs.items.ItemTagger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class AlternativeDurabilityLoss implements Listener {
   private static double durabilityLoss(ItemStack itemStack) {
      boolean isWeaponMaterial = EliteItemManager.isWeapon(itemStack);
      int maxDurability = ItemDurability.maximum(itemStack) > (isWeaponMaterial ? 2000 : 1000)
         ? (isWeaponMaterial ? 2000 : 1000)
         : ItemDurability.maximum(itemStack);
      double baseModifier = isWeaponMaterial ? 2000.0 : 1000.0;
      double durabilityLoss = (baseModifier - maxDurability) / baseModifier * ItemSettingsConfig.getEliteDurabilityMultiplier();
      double durabilityLevel = 1.0 + ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.UNBREAKING.getKey()) / 4.0;
      double defaultMultiplier = 0.5;
      return durabilityLoss / durabilityLevel * defaultMultiplier;
   }

   private static boolean isOnLastDamage(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (!itemStack.hasItemMeta()) {
         return false;
      } else if (!ItemTagger.isEliteItem(itemStack)) {
         return false;
      } else if (!(itemStack.getItemMeta() instanceof Damageable)) {
         return false;
      } else {
         return ItemDurability.maximum(itemStack) == 0 ? false : ((Damageable)itemStack.getItemMeta()).getDamage() + 1 >= ItemDurability.maximum(itemStack);
      }
   }

   public static void doDurabilityLoss(Player player) {
      if (ItemSettingsConfig.isEliteDurability()) {
         List<ItemStack> itemsList = new ArrayList<>(Arrays.stream(player.getInventory().getArmorContents()).toList());
         itemsList.add(player.getInventory().getItemInMainHand());
         itemsList.add(player.getInventory().getItemInOffHand());

         for (ItemStack itemStack : itemsList) {
            ItemDurability.prepareMagicWeapon(itemStack);
            if (itemStack != null
               && ItemDurability.maximum(itemStack) != 0
               && EliteItemManager.isEliteMobsItem(itemStack)
               && itemStack.getItemMeta() instanceof Damageable damageable) {
               int maxDurability = ItemDurability.maximum(itemStack);
               int durabilityLoss = (int)(maxDurability * durabilityLoss(itemStack));
               int currentDurability = damageable.getDamage();
               int newDurability = currentDurability + durabilityLoss;
               if (newDurability >= maxDurability) {
                  if (!ItemSettingsConfig.isPreventEliteItemsFromBreaking()) {
                     itemStack.setAmount(0);
                     continue;
                  }

                  newDurability = maxDurability - 1;
               }

               damageable.setDamage(newDurability);
               itemStack.setItemMeta(damageable);
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onDurabilityLoss(PlayerItemDamageEvent event) {
      if (EliteItemManager.isEliteMobsItem(event.getItem())) {
         if (EliteItemManager.isArmor(event.getItem()) || EliteItemManager.isWeapon(event.getItem())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      doDurabilityLoss(event.getEntity());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerShoot(EntityShootBowEvent event) {
      if (EliteItemManager.isEliteMobsItem(event.getBow())) {
         if (isOnLastDamage(event.getBow())) {
            event.setCancelled(true);
         }
      }
   }
}
