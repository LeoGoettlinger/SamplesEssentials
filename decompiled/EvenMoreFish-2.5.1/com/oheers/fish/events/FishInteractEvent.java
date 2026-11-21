package com.oheers.fish.events;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.fishing.items.FishManager;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FishInteractEvent implements Listener {
   @EventHandler
   public void interactEvent(PlayerInteractEvent event) {
      if (!event.getPlayer().isSneaking() && !EquipmentSlot.OFF_HAND.equals(event.getHand()) && event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
         ItemStack item = event.getItem();
         if (item != null) {
            IFish fish = FishManager.getInstance().getFish(item);
            if (fish != null) {
               List<Reward> rewards = fish.getInteractRewards();
               if (!rewards.isEmpty()) {
                  event.setCancelled(true);
                  event.getPlayer().getInventory().getItemInMainHand().setAmount(item.getAmount() - 1);
                  rewards.forEach(r -> r.give(event.getPlayer(), event.getPlayer().getLocation()));
               }
            }
         }
      }
   }
}
