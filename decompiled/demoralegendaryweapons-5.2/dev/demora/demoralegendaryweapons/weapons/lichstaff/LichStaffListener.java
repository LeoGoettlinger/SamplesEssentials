package dev.demora.demoralegendaryweapons.weapons.lichstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class LichStaffListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final LichStaff lichStaff;
   private final Map<UUID, BukkitRunnable> iceWalkingTasks;
   private final Set<UUID> shootingPlayers;

   public LichStaffListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.lichStaff = new LichStaff(plugin);
      this.iceWalkingTasks = new HashMap<>();
      this.shootingPlayers = new HashSet<>();
   }

   public LichStaff getLichStaff() {
      return this.lichStaff;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      final Player player = event.getPlayer();
      ItemStack item = player.getInventory().getItemInMainHand();
      if (this.lichStaff.isLichStaff(item)) {
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            if (this.shootingPlayers.contains(player.getUniqueId())) {
               return;
            }

            if (!this.lichStaff.canShoot(player)) {
               if (this.lichStaff.isOnCooldown(player)) {
                  long remaining = this.lichStaff.getRemainingCooldown(player);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.lich-staff-cooldown").replace("{time}", String.valueOf(remaining)));
               } else {
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.lich-staff-no-ammo"));
               }

               player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 0.8F);
               return;
            }

            this.shootingPlayers.add(player.getUniqueId());
            this.lichStaff.shoot(player);
            (new BukkitRunnable() {
               public void run() {
                  LichStaffListener.this.shootingPlayers.remove(player.getUniqueId());
               }
            }).runTaskLater(this.plugin, 5L);
         }
      }
   }

   @EventHandler
   public void onPlayerItemHeld(PlayerItemHeldEvent event) {
      Player player = event.getPlayer();
      ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
      this.stopIceWalking(player);
      if (this.lichStaff.isLichStaff(newItem)) {
         this.startIceWalking(player);
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         final Player player = (Player)event.getWhoClicked();
         ItemStack item = event.getCurrentItem();
         if (event.getSlotType() == SlotType.QUICKBAR && this.lichStaff.isLichStaff(item)) {
            (new BukkitRunnable() {
               public void run() {
                  ItemStack heldItem = player.getInventory().getItemInMainHand();
                  if (LichStaffListener.this.lichStaff.isLichStaff(heldItem)) {
                     LichStaffListener.this.startIceWalking(player);
                  } else {
                     LichStaffListener.this.stopIceWalking(player);
                  }
               }
            }).runTaskLater(this.plugin, 1L);
         }
      }
   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      if (this.lichStaff.isLichStaff(event.getItemDrop().getItemStack())) {
         this.stopIceWalking(player);
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      final Player player = event.getPlayer();
      (new BukkitRunnable() {
         public void run() {
            if (player.isOnline()) {
               ItemStack heldItem = player.getInventory().getItemInMainHand();
               if (LichStaffListener.this.lichStaff.isLichStaff(heldItem)) {
                  int ammo = LichStaffListener.this.lichStaff.getAmmo(player);
                  if (ammo == 0 && !LichStaffListener.this.lichStaff.isOnCooldown(player)) {
                     LichStaffListener.this.lichStaff.reloadAmmo(player);
                  }

                  LichStaffListener.this.startIceWalking(player);
               }
            }
         }
      }).runTaskLater(this.plugin, 20L);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.stopIceWalking(player);
      this.shootingPlayers.remove(player.getUniqueId());
   }

   private void startIceWalking(Player player) {
      this.stopIceWalking(player);
      BukkitRunnable task = new BukkitRunnable() {
         private int tickCount = 0;

         public void run() {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (LichStaffListener.this.lichStaff.isLichStaff(heldItem) && player.isOnline()) {
               Location playerLoc = player.getLocation();
               int playerX = playerLoc.getBlockX();
               int playerY = (int)Math.floor(playerLoc.getY()) - 1;
               int playerZ = playerLoc.getBlockZ();
               int radius = 3;
               if (this.tickCount % 4 == 0) {
                  Location particleLoc = playerLoc.clone().add(0.0, 1.0, 0.0);
                  playerLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0.1, 0.2, 0.1, 0.01);
               }

               this.tickCount++;

               for (int x = playerX - radius; x <= playerX + radius; x++) {
                  for (int z = playerZ - radius; z <= playerZ + radius; z++) {
                     double distance = Math.sqrt(Math.pow(x - playerX, 2.0) + Math.pow(z - playerZ, 2.0));
                     if (distance <= radius) {
                        Block block = playerLoc.getWorld().getBlockAt(x, playerY, z);
                        if (!LichStaffListener.this.plugin.isRegionProtected(block.getLocation())) {
                           Material type = block.getType();
                           if (type == Material.WATER) {
                              block.setType(Material.ICE);
                           } else if (type == Material.LAVA) {
                              block.setType(Material.OBSIDIAN);
                           }
                        }
                     }
                  }
               }
            } else {
               LichStaffListener.this.iceWalkingTasks.remove(player.getUniqueId());
               this.cancel();
            }
         }
      };
      this.iceWalkingTasks.put(player.getUniqueId(), task);
      task.runTaskTimer(this.plugin, 0L, 5L);
   }

   private void stopIceWalking(Player player) {
      BukkitRunnable task = this.iceWalkingTasks.remove(player.getUniqueId());
      if (task != null) {
         task.cancel();
      }
   }
}
