package com.Sw3ys.bigenderchest.listeners;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import com.Sw3ys.bigenderchest.manager.EnderChestManager;
import com.Sw3ys.bigenderchest.manager.LidAnimator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class EnderChestListener implements Listener {
   private final BiggerEnderChest plugin;
   private final EnderChestManager manager;
   private final LidAnimator animator;

   public EnderChestListener(BiggerEnderChest plugin, EnderChestManager manager, LidAnimator animator) {
      this.plugin = plugin;
      this.manager = manager;
      this.animator = animator;
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent e) {
      if (e.getHand() == EquipmentSlot.HAND) {
         if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            if (!player.isSneaking()) {
               Block block = e.getClickedBlock();
               if (block != null && block.getType() == Material.ENDER_CHEST) {
                  if (!player.hasPermission("enderchest.world.bypass")) {
                     List<String> disabledWorlds = this.plugin.getConfig().getStringList("disabled-worlds");
                     if (disabledWorlds.contains(player.getWorld().getName())) {
                        String message = this.plugin.getConfig().getString("messages.world-disabled", "&cYou cannot use Ender Chests in this world.");
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        e.setCancelled(true);
                        return;
                     }
                  }

                  e.setCancelled(true);
                  this.manager.open(player, player.getUniqueId());
                  this.animator.open(player, block);
               }
            }
         }
      }
   }
}
