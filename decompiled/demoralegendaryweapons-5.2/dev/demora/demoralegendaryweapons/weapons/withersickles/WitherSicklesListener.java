package dev.demora.demoralegendaryweapons.weapons.withersickles;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WitherSicklesListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final WitherSickles witherSickles;
   private final SickleCooldownManager cooldownManager;
   private final SickleInventoryManager inventoryManager;
   private final SickleThrowManager throwManager;
   private final SickleActionBarManager actionBarManager;
   private final Random random = new Random();

   public WitherSicklesListener(DemoraLegendaryWeapons plugin, WitherSickles witherSickles) {
      this.plugin = plugin;
      this.witherSickles = witherSickles;
      this.cooldownManager = new SickleCooldownManager(plugin);
      this.inventoryManager = new SickleInventoryManager(plugin, witherSickles, this.cooldownManager);
      this.throwManager = new SickleThrowManager(plugin, witherSickles, this.cooldownManager, this.inventoryManager);
      this.actionBarManager = new SickleActionBarManager(plugin, witherSickles, this.cooldownManager, this.inventoryManager);
      this.actionBarManager.startActionBarTask();
   }

   public SickleCooldownManager getCooldownManager() {
      return this.cooldownManager;
   }

   @EventHandler
   public void onItemHeld(PlayerItemHeldEvent event) {
      Player player = event.getPlayer();
      ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
      ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
      if (this.witherSickles.isWitherSickle(oldItem) && !this.witherSickles.isWitherSickle(newItem)) {
         this.inventoryManager.removeOffhandSickle(player);
      }

      if (this.witherSickles.isWitherSickle(newItem) && !this.cooldownManager.isOnCooldown(player)) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.inventoryManager.equipOffhandSickle(player), 1L);
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
   public void onDropItem(PlayerDropItemEvent event) {
      this.inventoryManager.handleDropItem(event);
   }

   @EventHandler
   public void onSwapHands(PlayerSwapHandItemsEvent event) {
      this.inventoryManager.handleSwapHands(event);
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player player = (Player)event.getDamager();
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         ItemStack offHand = player.getInventory().getItemInOffHand();
         if (!this.witherSickles.isWitherSickle(offHand)) {
            if (!this.witherSickles.isAdditionalSickle(mainHand)) {
               if (this.witherSickles.isWitherSickle(mainHand)) {
                  if (event.getEntity() instanceof LivingEntity) {
                     LivingEntity target = (LivingEntity)event.getEntity();
                     double hitChance = this.plugin.getConfig().getDouble("weapons.wither_sickles.wither_chance_hit", 0.1);
                     if (this.random.nextDouble() < hitChance) {
                        int duration = this.plugin.getConfig().getInt("weapons.wither_sickles.wither_duration_ticks", 60);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, 0, true, true));
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction().isRightClick()) {
         Player player = event.getPlayer();
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         ItemStack offHand = player.getInventory().getItemInOffHand();
         if (this.witherSickles.isWitherSickle(mainHand) && this.witherSickles.isAdditionalSickle(offHand)) {
            event.setCancelled(true);
            if (this.cooldownManager.isOnCooldown(player)) {
               long remaining = this.cooldownManager.getRemainingCooldown(player);
               player.sendMessage(
                  this.plugin.getMessageWithPrefix("weapon-messages.wither-sickles-throw-cooldown").replace("{time}", String.valueOf(remaining))
               );
            } else {
               player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
               this.inventoryManager.setOffhandSickle(player, false);
               this.cooldownManager.setCooldown(player);
               this.throwManager.throwSickle(player);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         if (this.witherSickles.isWitherSickle(mainHand) && !this.cooldownManager.isOnCooldown(player)) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.inventoryManager.equipOffhandSickle(player), 1L);
         }
      }
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      ItemStack off = player.getInventory().getItemInOffHand();
      if (this.witherSickles.isAdditionalSickle(off)) {
         player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
      }

      event.getDrops().removeIf(this.witherSickles::isAdditionalSickle);
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID id = event.getPlayer().getUniqueId();
      this.cooldownManager.removePlayerData(id);
      this.inventoryManager.removePlayerData(id);
   }
}
