package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class ReworkElytraListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final ReworkElytra reworkElytra;
   private final ReworkElytraTrackingManager trackingManager;
   private final ReworkElytraExplosionManager explosionManager;
   private final ReworkElytraBoostManager boostManager;
   private final ReworkElytraBossBarManager bossBarManager;
   private final ReworkElytraInventoryManager inventoryManager;

   public ReworkElytraListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.reworkElytra = new ReworkElytra(plugin);
      this.explosionManager = new ReworkElytraExplosionManager(plugin, this.reworkElytra);
      this.trackingManager = new ReworkElytraTrackingManager(plugin, this.reworkElytra, this.explosionManager, null);
      this.boostManager = new ReworkElytraBoostManager(plugin, this.reworkElytra);
      this.inventoryManager = new ReworkElytraInventoryManager(plugin, this.reworkElytra);
      this.bossBarManager = new ReworkElytraBossBarManager(plugin, this.reworkElytra, this.trackingManager);
      this.trackingManager.setBossBarManager(this.bossBarManager);
   }

   public ReworkElytra getReworkElytra() {
      return this.reworkElytra;
   }

   @EventHandler
   public void onExplosionDamageToSelf(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         ItemStack chest = player.getInventory().getChestplate();
         if (this.reworkElytra.isReworkElytra(chest)) {
            if (event.getCause() == DamageCause.FALL) {
               event.setCancelled(true);
            } else {
               if (this.explosionManager.isImmuneToLandingExplosion(player.getUniqueId()) && event.getCause() == DamageCause.ENTITY_EXPLOSION) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
      if (event.isSneaking()) {
         Player player = event.getPlayer();
         if (player.isGliding()) {
            ItemStack chest = player.getInventory().getChestplate();
            if (this.reworkElytra.isReworkElytra(chest)) {
               this.boostManager.activateBoost(player);
            }
         }
      }
   }

   @EventHandler
   public void onFireworkExplode(FireworkExplodeEvent event) {
      Firework firework = event.getEntity();
      if (firework.getShooter() instanceof Player) {
         Player player = (Player)firework.getShooter();
         ItemStack chest = player.getInventory().getChestplate();
         if (this.reworkElytra.isReworkElytra(chest) && player.isGliding()) {
            event.setCancelled(true);
            firework.remove();
         }
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (player.isGliding()) {
         ItemStack chest = player.getInventory().getChestplate();
         if (this.reworkElytra.isReworkElytra(chest)) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
               event.setCancelled(true);
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.rework-elytra-no-fireworks"));
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         ItemStack cursor = event.getCursor();
         ItemStack current = event.getCurrentItem();
         if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (current != null && current.getType() == Material.ELYTRA) {
               if (this.reworkElytra.isReworkElytra(current)) {
                  this.inventoryManager.scheduleRemoveNormalElytra(player);
               } else if (!this.plugin.getReinforcedElytra().isReinforcedElytra(current) && this.inventoryManager.playerHasReworkElytra(player)) {
                  event.setCancelled(true);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.rework-elytra-conflict"));
               }
            }
         } else {
            if (event.getSlotType() == SlotType.ARMOR) {
               if (cursor == null || cursor.getType() == Material.AIR || cursor.getType() != Material.ELYTRA) {
                  return;
               }

               if (this.reworkElytra.isReworkElytra(cursor)) {
                  this.inventoryManager.scheduleRemoveNormalElytra(player);
               } else if (!this.plugin.getReinforcedElytra().isReinforcedElytra(cursor) && this.inventoryManager.playerHasReworkElytra(player)) {
                  event.setCancelled(true);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.rework-elytra-conflict"));
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPickupItem(EntityPickupItemEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         ItemStack item = event.getItem().getItemStack();
         if (item.getType() == Material.ELYTRA) {
            boolean isRework = this.reworkElytra.isReworkElytra(item);
            if (!isRework
               && !this.plugin.getReinforcedElytra().isReinforcedElytra(item)
               && (this.reworkElytra.isReworkElytra(player.getInventory().getChestplate()) || this.inventoryManager.playerHasReworkElytra(player))) {
               this.inventoryManager.scheduleRemoveNormalElytra(player);
            }

            if (isRework) {
               ItemStack chest = player.getInventory().getChestplate();
               boolean wearingNormal = chest != null
                  && chest.getType() == Material.ELYTRA
                  && !this.reworkElytra.isReworkElytra(chest)
                  && !this.plugin.getReinforcedElytra().isReinforcedElytra(chest);
               if (wearingNormal || this.inventoryManager.hasNormalElytraInStorage(player)) {
                  this.inventoryManager.scheduleRemoveNormalElytra(player);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      UUID uid = player.getUniqueId();
      this.trackingManager.cleanupPlayer(uid);
      this.explosionManager.cleanupPlayer(uid);
      this.boostManager.cleanupPlayer(uid);
      this.bossBarManager.cleanupPlayer(uid);
      this.reworkElytra.removePlayerData(uid);
   }
}
