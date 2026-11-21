package com.viaversion.viabackwards.listener;

import com.viaversion.viabackwards.BukkitPlugin;
import com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.Protocol1_21_11To1_21_9;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class SpearAttack1_21_11 extends ViaBukkitListener {
   public SpearAttack1_21_11(BukkitPlugin plugin) {
      super(plugin, Protocol1_21_11To1_21_9.class);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      if (this.isOnPipe(player)) {
         ItemStack item = player.getInventory().getItemInMainHand();
         if (item.getType().name().endsWith("_SPEAR")) {
            event.setCancelled(true);
         }
      }
   }
}
