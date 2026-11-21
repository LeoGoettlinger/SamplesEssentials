package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra;

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

public class ReinforcedElytraListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final ReinforcedElytra reinforcedElytra;
   private final ReinforcedElytraTrackingManager trackingManager;
   private final ReinforcedElytraExplosionManager explosionManager;
   private final ReinforcedElytraBoostManager boostManager;
   private final ReinforcedElytraBossBarManager bossBarManager;
   private final ReinforcedElytraInventoryManager inventoryManager;

   public ReinforcedElytraListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.reinforcedElytra = new ReinforcedElytra(plugin);
      this.explosionManager = new ReinforcedElytraExplosionManager(plugin, this.reinforcedElytra);
      this.trackingManager = new ReinforcedElytraTrackingManager(plugin, this.reinforcedElytra, this.explosionManager, null);
      this.boostManager = new ReinforcedElytraBoostManager(plugin, this.reinforcedElytra);
      this.inventoryManager = new ReinforcedElytraInventoryManager(plugin, this.reinforcedElytra);
      this.bossBarManager = new ReinforcedElytraBossBarManager(plugin, this.reinforcedElytra, this.trackingManager);
      this.trackingManager.setBossBarManager(this.bossBarManager);
   }

   public ReinforcedElytra getReinforcedElytra() {
      return this.reinforcedElytra;
   }

   @EventHandler
   public void onExplosionDamageToSelf(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         ItemStack chest = player.getInventory().getChestplate();
         if (this.reinforcedElytra.isReinforcedElytra(chest)) {
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
            if (this.reinforcedElytra.isReinforcedElytra(chest)) {
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
         if (this.reinforcedElytra.isReinforcedElytra(chest) && player.isGliding()) {
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
         if (this.reinforcedElytra.isReinforcedElytra(chest)) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
               event.setCancelled(true);
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.reinforced-elytra-no-fireworks"));
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
               if (this.reinforcedElytra.isReinforcedElytra(current)) {
                  this.inventoryManager.scheduleRemoveNormalElytra(player);
               } else if (this.inventoryManager.playerHasReinforcedElytra(player)) {
                  event.setCancelled(true);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.reinforced-elytra-conflict"));
               }
            }
         } else {
            if (event.getSlotType() == SlotType.ARMOR) {
               if (cursor == null || cursor.getType() == Material.AIR || cursor.getType() != Material.ELYTRA) {
                  return;
               }

               if (this.reinforcedElytra.isReinforcedElytra(cursor)) {
                  this.inventoryManager.scheduleRemoveNormalElytra(player);
               } else if (this.inventoryManager.playerHasReinforcedElytra(player)) {
                  event.setCancelled(true);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.reinforced-elytra-conflict"));
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
            boolean isReinforced = this.reinforcedElytra.isReinforcedElytra(item);
            if (!isReinforced
               && (this.reinforcedElytra.isReinforcedElytra(player.getInventory().getChestplate()) || this.inventoryManager.playerHasReinforcedElytra(player))) {
               this.inventoryManager.scheduleRemoveNormalElytra(player);
            }

            if (isReinforced) {
               ItemStack chest = player.getInventory().getChestplate();
               boolean wearingNormal = chest != null && chest.getType() == Material.ELYTRA && !this.reinforcedElytra.isReinforcedElytra(chest);
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
      this.reinforcedElytra.removePlayerData(uid);
   }
}
