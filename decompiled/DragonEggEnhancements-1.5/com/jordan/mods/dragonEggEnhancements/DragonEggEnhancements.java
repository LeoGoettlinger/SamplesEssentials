package com.jordan.mods.dragonEggEnhancements;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonEggEnhancements extends JavaPlugin implements Listener {
   private int updateNotifier;
   private HashMap<UUID, Boolean> playersWithEgg = new HashMap<>();
   private Location droppedEggLocation = null;
   private Item droppedEggItem = null;
   private Location placedEggLocation = null;

   public void onEnable() {
      this.updateNotifier = this.setupUpdateNotifier();
      this.saveDefaultConfig();
      Bukkit.getPluginManager().registerEvents(this, this);
      (new BukkitRunnable() {
         public void run() {
            DragonEggEnhancements.this.checkPlayersWithDragonEgg();
         }
      }).runTaskTimer(this, 0L, 20L);
      (new BukkitRunnable() {
         public void run() {
            DragonEggEnhancements.this.checkDroppedEggs();
            DragonEggEnhancements.this.checkPlacedEggs();
            DragonEggEnhancements.this.checkVoidEgg();
         }
      }).runTaskTimer(this, 0L, 10L);
      this.getLogger().info("Dragon Egg Enhancements has been enabled!");
   }

   public void onDisable() {
      this.getLogger().info("Dragon Egg Enhancements has been disabled!");
   }

   public void saveDefaultConfig() {
      FileConfiguration config = this.getConfig();
      config.addDefault("effects.speed.enabled", true);
      config.addDefault("effects.speed.level", 1);
      config.addDefault("effects.fire_resistance.enabled", true);
      config.addDefault("effects.fire_resistance.level", 1);
      config.addDefault("effects.strength.enabled", true);
      config.addDefault("effects.strength.level", 1);
      config.addDefault("effects.glowing.enabled", true);
      config.addDefault("health.enabled", true);
      config.addDefault("health.hearts", 15);
      config.addDefault("protection.explosion_proof", true);
      config.addDefault("protection.fire_proof", true);
      config.addDefault("protection.cactus_proof", true);
      config.addDefault("protection.kill_command_proof", true);
      config.addDefault("protection.void_protection", true);
      config.addDefault("protection.no_despawn", true);
      config.addDefault("protection.block_containers", true);
      config.addDefault("display.show_coordinates_when_dropped", true);
      config.addDefault("display.glowing_when_dropped", true);
      config.addDefault("messages.egg_obtained", "§6You now possess the Dragon Egg! Its power flows through you.");
      config.addDefault("messages.egg_lost", "§cThe Dragon Egg's power has left you.");
      config.addDefault("messages.cannot_store", "§cThe Dragon Egg cannot be stored in containers!");
      config.addDefault("messages.egg_location", "§eThe Dragon Egg is dropped at: §6X: %x%, Y: %y%, Z: %z% §ein world §6%world%");
      config.addDefault("messages.egg_location_placed", "§eThe Dragon Egg is placed at: §6X: %x%, Y: %y%, Z: %z% §ein world §6%world%");
      config.addDefault("messages.egg_placed", "§eYou placed the Dragon Egg at: §6X: %x%, Y: %y%, Z: %z%");
      config.addDefault("messages.egg_not_found", "§cThe Dragon Egg is not currently dropped or placed in the world.");
      config.addDefault("messages.void_return", "§eThe Dragon Egg has returned to spawn after falling into the void!");
      config.options().copyDefaults(true);
      super.saveDefaultConfig();
   }

   private void checkPlayersWithDragonEgg() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         boolean hasEgg = player.getInventory().contains(Material.DRAGON_EGG);
         UUID uuid = player.getUniqueId();
         Boolean hadEgg = this.playersWithEgg.get(uuid);
         if (!hasEgg || hadEgg != null && hadEgg) {
            if (!hasEgg && hadEgg != null && hadEgg) {
               this.playersWithEgg.put(uuid, false);
               this.removeEffects(player);
               String message = this.getConfig().getString("messages.egg_lost");
               if (message != null && !message.isEmpty()) {
                  player.sendMessage(message);
               }
            } else if (hasEgg) {
               this.applyEffects(player);
            }
         } else {
            this.playersWithEgg.put(uuid, true);
            this.applyEffects(player);
            String message = this.getConfig().getString("messages.egg_obtained");
            if (message != null && !message.isEmpty()) {
               player.sendMessage(message);
            }
         }
      }
   }

   private void applyEffects(Player player) {
      FileConfiguration config = this.getConfig();
      if (config.getBoolean("effects.speed.enabled")) {
         int level = config.getInt("effects.speed.level") - 1;
         player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, level, false, false, true));
      }

      if (config.getBoolean("effects.fire_resistance.enabled")) {
         int level = config.getInt("effects.fire_resistance.level") - 1;
         player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, level, false, false, true));
      }

      if (config.getBoolean("effects.strength.enabled")) {
         int level = config.getInt("effects.strength.level") - 1;
         player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, level, false, false, true));
      }

      if (config.getBoolean("effects.glowing.enabled")) {
         player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false, true));
      }

      if (config.getBoolean("health.enabled")) {
         double hearts = config.getDouble("health.hearts");
         AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
         if (healthAttr != null) {
            healthAttr.setBaseValue(hearts * 2.0);
            if (player.getHealth() > hearts * 2.0) {
               player.setHealth(hearts * 2.0);
            }
         }
      }
   }

   private void removeEffects(Player player) {
      player.removePotionEffect(PotionEffectType.SPEED);
      player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
      player.removePotionEffect(PotionEffectType.STRENGTH);
      player.removePotionEffect(PotionEffectType.GLOWING);
      AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
      if (healthAttr != null) {
         healthAttr.setBaseValue(20.0);
         if (player.getHealth() > 20.0) {
            player.setHealth(20.0);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (this.getConfig().getBoolean("protection.block_containers")) {
         ItemStack item = event.getCurrentItem();
         ItemStack cursor = event.getCursor();
         if (item != null && item.getType() == Material.DRAGON_EGG && event.getClickedInventory() != null) {
            InventoryType type = event.getClickedInventory().getType();
            if (type != InventoryType.PLAYER && type != InventoryType.CRAFTING) {
               event.setCancelled(true);
               if (event.getWhoClicked() instanceof Player) {
                  Player player = (Player)event.getWhoClicked();
                  String message = this.getConfig().getString("messages.cannot_store");
                  if (message != null && !message.isEmpty()) {
                     player.sendMessage(message);
                  }
               }

               return;
            }
         }

         if (cursor != null && cursor.getType() == Material.DRAGON_EGG && event.getClickedInventory() != null) {
            InventoryType type = event.getClickedInventory().getType();
            if (type != InventoryType.PLAYER && type != InventoryType.CRAFTING) {
               event.setCancelled(true);
               if (event.getWhoClicked() instanceof Player) {
                  Player player = (Player)event.getWhoClicked();
                  String message = this.getConfig().getString("messages.cannot_store");
                  if (message != null && !message.isEmpty()) {
                     player.sendMessage(message);
                  }
               }

               return;
            }
         }

         if (event.isShiftClick() && item != null && item.getType() == Material.DRAGON_EGG) {
            Inventory clickedInv = event.getClickedInventory();
            if (clickedInv != null && clickedInv.getType() == InventoryType.PLAYER) {
               event.setCancelled(true);
               if (event.getWhoClicked() instanceof Player) {
                  Player player = (Player)event.getWhoClicked();
                  String message = this.getConfig().getString("messages.cannot_store");
                  if (message != null && !message.isEmpty()) {
                     player.sendMessage(message);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryDrag(InventoryDragEvent event) {
      if (this.getConfig().getBoolean("protection.block_containers")) {
         ItemStack item = event.getOldCursor();
         if (item != null && item.getType() == Material.DRAGON_EGG) {
            for (int slot : event.getRawSlots()) {
               Inventory inv = event.getView().getInventory(slot);
               if (inv != null && inv.getType() != InventoryType.PLAYER && inv.getType() != InventoryType.CRAFTING) {
                  event.setCancelled(true);
                  if (event.getWhoClicked() instanceof Player) {
                     Player player = (Player)event.getWhoClicked();
                     String message = this.getConfig().getString("messages.cannot_store");
                     if (message != null && !message.isEmpty()) {
                        player.sendMessage(message);
                     }
                  }

                  return;
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryMoveItem(InventoryMoveItemEvent event) {
      if (this.getConfig().getBoolean("protection.block_containers")) {
         ItemStack item = event.getItem();
         if (item != null && item.getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onItemDespawn(ItemDespawnEvent event) {
      if (this.getConfig().getBoolean("protection.no_despawn")) {
         if (event.getEntity().getItemStack().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityDeath(EntityDeathEvent event) {
      if (this.getConfig().getBoolean("protection.kill_command_proof")) {
         if (event.getEntity() instanceof Item) {
            Item item = (Item)event.getEntity();
            if (item.getItemStack().getType() == Material.DRAGON_EGG) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Item) {
         Item item = (Item)event.getEntity();
         if (item.getItemStack().getType() == Material.DRAGON_EGG) {
            FileConfiguration config = this.getConfig();
            DamageCause cause = event.getCause();
            if (config.getBoolean("protection.explosion_proof") && (cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION)) {
               event.setCancelled(true);
            }

            if (config.getBoolean("protection.fire_proof") && (cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK || cause == DamageCause.LAVA)) {
               event.setCancelled(true);
            }

            if (config.getBoolean("protection.cactus_proof") && cause == DamageCause.CONTACT) {
               event.setCancelled(true);
            }

            if (config.getBoolean("protection.kill_command_proof") && cause == DamageCause.VOID) {
               event.setCancelled(true);
            }

            if (config.getBoolean("protection.void_protection") && cause == DamageCause.VOID) {
               event.setCancelled(true);
               World world = item.getWorld();
               Location spawn = world.getSpawnLocation();
               item.teleport(spawn);
               String message = config.getString("messages.void_return");
               if (message != null && !message.isEmpty()) {
                  Bukkit.broadcastMessage(message);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      if (event.getItemDrop().getItemStack().getType() == Material.DRAGON_EGG) {
         this.droppedEggItem = event.getItemDrop();
         this.droppedEggLocation = event.getItemDrop().getLocation();
         this.placedEggLocation = null;
         if (this.getConfig().getBoolean("display.glowing_when_dropped")) {
            this.droppedEggItem.setGlowing(true);
         }

         if (this.getConfig().getBoolean("display.show_coordinates_when_dropped")) {
            Location loc = this.droppedEggLocation;
            String message = this.getConfig()
               .getString("messages.egg_location")
               .replace("%x%", String.valueOf(loc.getBlockX()))
               .replace("%y%", String.valueOf(loc.getBlockY()))
               .replace("%z%", String.valueOf(loc.getBlockZ()))
               .replace("%world%", loc.getWorld().getName());
            event.getPlayer().sendMessage(message);
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (event.getBlock().getType() == Material.DRAGON_EGG) {
         this.placedEggLocation = event.getBlock().getLocation();
         this.droppedEggItem = null;
         this.droppedEggLocation = null;
         if (this.getConfig().getBoolean("display.show_coordinates_when_dropped")) {
            Location loc = this.placedEggLocation;
            String message = this.getConfig()
               .getString("messages.egg_placed")
               .replace("%x%", String.valueOf(loc.getBlockX()))
               .replace("%y%", String.valueOf(loc.getBlockY()))
               .replace("%z%", String.valueOf(loc.getBlockZ()))
               .replace("%world%", loc.getWorld().getName());
            event.getPlayer().sendMessage(message);
         }
      }
   }

   private void checkDroppedEggs() {
      for (World world : Bukkit.getWorlds()) {
         for (Item item : world.getEntitiesByClass(Item.class)) {
            if (item.getItemStack().getType() == Material.DRAGON_EGG) {
               this.droppedEggItem = item;
               this.droppedEggLocation = item.getLocation();
               if (this.getConfig().getBoolean("display.glowing_when_dropped")) {
                  item.setGlowing(true);
               }

               return;
            }
         }
      }

      if (this.placedEggLocation == null) {
         this.droppedEggItem = null;
         this.droppedEggLocation = null;
      }
   }

   private void checkPlacedEggs() {
      if (this.placedEggLocation != null) {
         Block block = this.placedEggLocation.getBlock();
         if (block.getType() == Material.DRAGON_EGG) {
            return;
         }

         this.placedEggLocation = null;
      }
   }

   private void checkVoidEgg() {
      if (this.getConfig().getBoolean("protection.void_protection")) {
         if (this.droppedEggItem != null && this.droppedEggItem.isValid()) {
            Location loc = this.droppedEggItem.getLocation();
            if (loc.getY() < loc.getWorld().getMinHeight()) {
               World world = this.droppedEggItem.getWorld();
               Location spawn = world.getSpawnLocation();
               this.droppedEggItem.teleport(spawn);
               this.droppedEggLocation = spawn;
               String message = this.getConfig().getString("messages.void_return");
               if (message != null && !message.isEmpty()) {
                  Bukkit.broadcastMessage(message);
               }
            }
         }
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!command.getName().equalsIgnoreCase("dragoneggcoords") && !command.getName().equalsIgnoreCase("eggcoords")) {
         return false;
      } else if (this.placedEggLocation != null) {
         Location loc = this.placedEggLocation;
         String message = this.getConfig()
            .getString("messages.egg_location_placed")
            .replace("%x%", String.valueOf(loc.getBlockX()))
            .replace("%y%", String.valueOf(loc.getBlockY()))
            .replace("%z%", String.valueOf(loc.getBlockZ()))
            .replace("%world%", loc.getWorld().getName());
         sender.sendMessage(message);
         return true;
      } else if (this.droppedEggLocation != null && this.droppedEggItem != null && this.droppedEggItem.isValid()) {
         Location loc = this.droppedEggLocation;
         String message = this.getConfig()
            .getString("messages.egg_location")
            .replace("%x%", String.valueOf(loc.getBlockX()))
            .replace("%y%", String.valueOf(loc.getBlockY()))
            .replace("%z%", String.valueOf(loc.getBlockZ()))
            .replace("%world%", loc.getWorld().getName());
         sender.sendMessage(message);
         return true;
      } else {
         String message = this.getConfig().getString("messages.egg_not_found");
         sender.sendMessage(message);
         return true;
      }
   }
}
