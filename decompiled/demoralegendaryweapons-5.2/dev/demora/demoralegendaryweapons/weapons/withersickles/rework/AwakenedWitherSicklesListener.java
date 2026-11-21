package dev.demora.demoralegendaryweapons.weapons.withersickles.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class AwakenedWitherSicklesListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final AwakenedWitherSickles awakenedSickles;
   private final AwakenedSickleInventoryManager inventoryManager;
   private final AwakenedSickleCooldownManager cooldownManager;
   private final AwakenedSickleThrowManager throwManager;
   private final AwakenedSickleActionBarManager actionBarManager;
   private final AwakenedSickleShieldManager shieldManager;

   public AwakenedWitherSicklesListener(DemoraLegendaryWeapons plugin, AwakenedWitherSickles awakenedSickles) {
      this.plugin = plugin;
      this.awakenedSickles = awakenedSickles;
      this.cooldownManager = new AwakenedSickleCooldownManager(plugin);
      this.inventoryManager = new AwakenedSickleInventoryManager(plugin, awakenedSickles);
      this.throwManager = new AwakenedSickleThrowManager(plugin, awakenedSickles, this.cooldownManager, this.inventoryManager);
      this.actionBarManager = new AwakenedSickleActionBarManager(plugin, awakenedSickles, this.cooldownManager, this.inventoryManager);
      this.shieldManager = new AwakenedSickleShieldManager(plugin);
      this.actionBarManager.startActionBarTask();
   }

   public AwakenedSickleCooldownManager getCooldownManager() {
      return this.cooldownManager;
   }

   @EventHandler
   public void onItemHeld(PlayerItemHeldEvent event) {
      Player player = event.getPlayer();
      ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
      ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
      if (this.awakenedSickles.isAwakenedWitherSickle(oldItem)) {
         this.inventoryManager.removeOffhandSickle(player);
      }

      if (this.awakenedSickles.isAwakenedWitherSickle(newItem) && !this.cooldownManager.isOnCooldown(player)) {
         this.inventoryManager.equipOffhandSickle(player);
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction().isRightClick()) {
         Player player = event.getPlayer();
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         ItemStack offHand = player.getInventory().getItemInOffHand();
         if (this.awakenedSickles.isAwakenedWitherSickle(mainHand)) {
            if (this.awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
               event.setCancelled(true);
               if (player.isSneaking()) {
                  this.shieldManager.activateShield(player);
               } else if (this.cooldownManager.isOnCooldown(player)) {
                  long remaining = this.cooldownManager.getRemainingCooldown(player);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.awakened-sickles-cooldown").replace("{time}", String.valueOf(remaining)));
               } else {
                  this.inventoryManager.removeOffhandSickle(player);
                  this.throwManager.throwSickle(player);
                  this.cooldownManager.setCooldown(player);
               }
            }
         }
      }
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         if (event.getEntity() instanceof LivingEntity) {
            Player player = (Player)event.getDamager();
            LivingEntity target = (LivingEntity)event.getEntity();
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (this.awakenedSickles.isAwakenedWitherSickle(mainHand)) {
               int fireDuration = this.plugin.getReworkConfig().getInt("awakened_wither_sickles.fire_duration_ticks", 100);
               target.setFireTicks(fireDuration);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      this.inventoryManager.handleInventoryClick(event);
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      this.inventoryManager.handleInventoryDrag(event);
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         if (this.awakenedSickles.isAwakenedWitherSickle(mainHand) && !this.cooldownManager.isOnCooldown(player)) {
            this.inventoryManager.equipOffhandSickle(player);
         }
      }
   }

   @EventHandler
   public void onSwapHands(PlayerSwapHandItemsEvent event) {
      this.inventoryManager.handleSwapHands(event);
   }

   @EventHandler
   public void onDropItem(PlayerDropItemEvent event) {
      this.inventoryManager.handleDropItem(event);
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (this.shieldManager.isShieldActive(player)) {
            double reduction = this.shieldManager.getDamageReduction();
            double newDamage = event.getDamage() * (1.0 - reduction);
            event.setDamage(newDamage);
            player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.05);
         }
      }
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      event.getDrops().removeIf(item -> this.awakenedSickles.isAdditionalAwakenedSickle(item));
      this.shieldManager.removePlayerData(event.getEntity().getUniqueId());
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.inventoryManager.removeOffhandSickle(player);
      this.cooldownManager.removePlayerData(player.getUniqueId());
      this.shieldManager.removePlayerData(player.getUniqueId());
   }

   public void reloadConfig() {
      this.shieldManager.reloadConfig();
   }
}
