package org.evenmorefish.fish.libs.dimensionfishing.common;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.dimensionfishing.util.Keys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FishingListener implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onFish(PlayerFishEvent event) {
      if (!event.isCancelled()) {
         switch (event.getState()) {
            case FISHING:
               int lureLevel = this.fetchLureLevel(event.getPlayer(), event.getHand());
               TrackedHook hook = new TrackedHook(event.getPlayer(), event.getHook(), lureLevel);
               if (!hook.isShouldCustomTick()) {
                  return;
               }

               HookManager.getInstance().trackHook(hook);
               break;
            case CAUGHT_ENTITY:
               HookManager.getInstance().checkCaughtEntity(event);
         }
      }
   }

   @EventHandler
   public void onHookRemoved(EntityRemoveFromWorldEvent event) {
      if (event.getEntity() instanceof FishHook hook) {
         Entity hooked = hook.getHookedEntity();
         if (hooked != null && hooked.getPersistentDataContainer().has(Keys.STAND_KEY)) {
            hooked.remove();
         }
      }
   }

   private int fetchLureLevel(@NotNull Player player, @Nullable EquipmentSlot slot) {
      if (slot == null) {
         return 0;
      } else {
         ItemStack item = player.getInventory().getItem(slot);
         return item.getEnchantmentLevel(Enchantment.LURE);
      }
   }
}
