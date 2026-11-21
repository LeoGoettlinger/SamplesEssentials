package dev.demora.demoralegendaryweapons.weapons.freezingchakram;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

public class FreezingChakramListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final FreezingChakram chakram;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Map<UUID, Boolean> hasOffhandChakram = new HashMap<>();
   private final Map<UUID, ItemDisplay> thrownChakrams = new HashMap<>();
   private final Map<UUID, Integer> cooldownEndTasks = new HashMap<>();
   private final Map<UUID, Integer> freezeHoldTasks = new HashMap<>();
   private final Map<UUID, ItemStack> storedOffhandItems = new HashMap<>();

   public FreezingChakramListener(DemoraLegendaryWeapons plugin, FreezingChakram chakram) {
      this.plugin = plugin;
      this.chakram = chakram;
   }

   @EventHandler
   public void onItemHeld(PlayerItemHeldEvent event) {
      Player player = event.getPlayer();
      ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
      ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
      if (this.chakram.isFreezingChakram(oldItem) && !this.chakram.isFreezingChakram(newItem)) {
         this.removeOffhandChakram(player);
      }

      if (this.chakram.isFreezingChakram(newItem) && !this.isOnCooldown(player)) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.equipOffhandChakram(player), 1L);
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         if (event.getAction().isRightClick()) {
            Player player = event.getPlayer();
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (this.chakram.isFreezingChakram(mainHand) && this.chakram.isAdditionalChakram(offHand)) {
               event.setCancelled(true);
               if (this.isOnCooldown(player)) {
                  long remaining = this.getRemainingCooldown(player);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.freezing-chakram-cooldown").replace("{time}", String.valueOf(remaining)));
               } else {
                  player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                  this.hasOffhandChakram.put(player.getUniqueId(), false);
                  this.throwChakram(player);
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         if (this.chakram.isFreezingChakram(mainHand) && !this.isOnCooldown(player)) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.equipOffhandChakram(player), 1L);
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         if (this.chakram.isFreezingChakram(player.getInventory().getItemInMainHand())) {
            if (event.getSlot() == 40 && this.chakram.isAdditionalChakram(event.getCurrentItem())) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         if (this.chakram.isFreezingChakram(player.getInventory().getItemInMainHand())) {
            if (event.getRawSlots().contains(45)) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onDropItem(PlayerDropItemEvent event) {
      ItemStack dropped = event.getItemDrop().getItemStack();
      if (this.chakram.isFreezingChakram(dropped)) {
         Player player = event.getPlayer();
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.removeOffhandChakram(player), 1L);
      } else {
         if (this.chakram.isAdditionalChakram(dropped)) {
            event.setCancelled(true);
            event.getItemDrop().remove();
         }
      }
   }

   @EventHandler
   public void onPickupItem(EntityPickupItemEvent event) {
      if (event.getEntity() instanceof Player player) {
         if (this.chakram.isFreezingChakram(event.getItem().getItemStack())) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               if (player.isOnline()) {
                  if (!this.isOnCooldown(player)) {
                     if (!this.thrownChakrams.containsKey(player.getUniqueId())) {
                        ItemStack mainHand = player.getInventory().getItemInMainHand();
                        if (this.chakram.isFreezingChakram(mainHand)) {
                           this.equipOffhandChakram(player);
                        }
                     }
                  }
               }
            }, 1L);
         }
      }
   }

   @EventHandler
   public void onSwapHands(PlayerSwapHandItemsEvent event) {
      Player player = event.getPlayer();
      if (this.chakram.isFreezingChakram(player.getInventory().getItemInMainHand()) || this.chakram.isAdditionalChakram(event.getOffHandItem())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player player = (Player)event.getDamager();
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         ItemStack offHand = player.getInventory().getItemInOffHand();
         if (!this.chakram.isAdditionalChakram(mainHand) && !this.chakram.isAdditionalChakram(offHand)) {
            if (this.chakram.isFreezingChakram(mainHand)) {
               if (event.getEntity() instanceof LivingEntity) {
                  LivingEntity target = (LivingEntity)event.getEntity();
                  double freezeChance = this.plugin.getConfig().getDouble("weapons.freezing_chakram.freeze_chance_hit", 0.15);
                  if (Math.random() < freezeChance) {
                     this.applyNativeFreeze(target);
                     target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 2.0F);
                     target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0.0, 1.0, 0.0), 20, 0.5, 0.5, 0.5, 0.1);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      this.removeOffhandChakram(player);
      this.cooldowns.remove(playerId);
      this.hasOffhandChakram.remove(playerId);
      Integer cooldownTask = this.cooldownEndTasks.remove(playerId);
      if (cooldownTask != null) {
         Bukkit.getScheduler().cancelTask(cooldownTask);
      }

      ItemDisplay thrown = this.thrownChakrams.remove(playerId);
      if (thrown != null && !thrown.isDead()) {
         thrown.remove();
      }
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      LivingEntity entity = event.getEntity();
      Integer taskId = this.freezeHoldTasks.remove(entity.getUniqueId());
      if (taskId != null) {
         Bukkit.getScheduler().cancelTask(taskId);
      }
   }

   private void equipOffhandChakram(Player player) {
      if (!this.hasOffhandChakram.getOrDefault(player.getUniqueId(), false)) {
         ItemStack offHand = player.getInventory().getItemInOffHand();
         if (this.chakram.isAdditionalChakram(offHand)) {
            this.hasOffhandChakram.put(player.getUniqueId(), true);
         } else {
            if (offHand != null && offHand.getType() != Material.AIR) {
               this.storedOffhandItems.put(player.getUniqueId(), offHand.clone());
            }

            player.getInventory().setItemInOffHand(this.chakram.createAdditionalChakram());
            this.hasOffhandChakram.put(player.getUniqueId(), true);
         }
      }
   }

   private void removeOffhandChakram(Player player) {
      UUID playerId = player.getUniqueId();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      if (this.chakram.isAdditionalChakram(offHand)) {
         player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
      }

      ItemStack stored = this.storedOffhandItems.remove(playerId);
      if (stored != null) {
         ItemStack currentOffHand = player.getInventory().getItemInOffHand();
         if (currentOffHand != null && currentOffHand.getType() != Material.AIR) {
            player.getInventory().addItem(new ItemStack[]{stored});
         } else {
            player.getInventory().setItemInOffHand(stored);
         }
      }

      this.hasOffhandChakram.put(playerId, false);
   }

   private void throwChakram(Player player) {
      final Location eyeLoc = player.getEyeLocation();
      final Vector direction = eyeLoc.getDirection().normalize();
      final ItemDisplay chakramDisplay = (ItemDisplay)player.getWorld().spawnEntity(eyeLoc, EntityType.ITEM_DISPLAY);
      ItemStack displayItem = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta displayMeta = displayItem.getItemMeta();
      if (displayMeta != null) {
         displayMeta.setCustomModelData(13);
         displayItem.setItemMeta(displayMeta);
      }

      chakramDisplay.setItemStack(displayItem);

      try {
         chakramDisplay.setInterpolationDelay(0);
         chakramDisplay.setInterpolationDuration(1);
         chakramDisplay.setTeleportDuration(1);
      } catch (NoSuchMethodError var13) {
      }

      this.thrownChakrams.put(player.getUniqueId(), chakramDisplay);
      player.getWorld().playSound(eyeLoc, Sound.ENTITY_SNOWBALL_THROW, 1.0F, 0.8F);
      final double flightSpeed = this.plugin.getConfig().getDouble("weapons.freezing_chakram.flight_speed", 1.5);
      final double returnSpeed = this.plugin.getConfig().getDouble("weapons.freezing_chakram.return_speed", 1.8);
      final double maxFlightDistance = this.plugin.getConfig().getDouble("weapons.freezing_chakram.max_flight_distance", 28.0);
      (new BukkitRunnable() {
         private Location currentLoc = eyeLoc.clone();
         private boolean isReturning = false;
         private float rotationAngle = 0.0F;
         private Vector currentDirection = direction.clone();

         public void run() {
            if (!chakramDisplay.isDead() && chakramDisplay.isValid()) {
               this.rotationAngle += 30.0F;
               if (!this.isReturning) {
                  this.currentDirection = direction.clone();
                  this.currentLoc.add(this.currentDirection.clone().multiply(flightSpeed));
                  chakramDisplay.teleport(this.currentLoc);
                  FreezingChakramListener.this.applySpinAroundOwnAxis(chakramDisplay, this.currentDirection, this.rotationAngle);
                  this.currentLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, this.currentLoc, 3, 0.2, 0.2, 0.2, 0.01);
                  if (!this.currentLoc.getBlock().isPassable() || this.currentLoc.distanceSquared(eyeLoc) >= maxFlightDistance * maxFlightDistance) {
                     this.isReturning = true;
                     return;
                  }

                  for (Entity entity : this.currentLoc.getWorld().getNearbyEntities(this.currentLoc, 1.0, 1.0, 1.0)) {
                     if (entity instanceof LivingEntity && entity != player && entity != chakramDisplay) {
                        FreezingChakramListener.this.hitTarget((LivingEntity)entity, player);
                        this.isReturning = true;
                        return;
                     }
                  }
               } else {
                  Vector returnVector = player.getLocation().add(0.0, 1.0, 0.0).subtract(this.currentLoc).toVector().normalize().multiply(returnSpeed);
                  this.currentDirection = returnVector.clone().normalize();
                  this.currentLoc.add(returnVector);
                  chakramDisplay.teleport(this.currentLoc);
                  FreezingChakramListener.this.applySpinAroundOwnAxis(chakramDisplay, this.currentDirection, this.rotationAngle);

                  for (Entity entityx : this.currentLoc.getWorld().getNearbyEntities(this.currentLoc, 1.0, 1.0, 1.0)) {
                     if (entityx instanceof LivingEntity && entityx != player && entityx != chakramDisplay) {
                        FreezingChakramListener.this.hitTarget((LivingEntity)entityx, player);
                     }
                  }

                  if (this.currentLoc.distance(player.getLocation()) < 1.5) {
                     player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.2F);
                     if (player.isOnline()) {
                        player.getInventory().setItemInOffHand(FreezingChakramListener.this.chakram.createAdditionalChakram());
                        FreezingChakramListener.this.hasOffhandChakram.put(player.getUniqueId(), true);
                        FreezingChakramListener.this.setCooldown(player);
                     }

                     this.cleanup();
                     return;
                  }
               }
            } else {
               this.cleanup();
            }
         }

         private void cleanup() {
            if (!chakramDisplay.isDead()) {
               chakramDisplay.remove();
            }

            FreezingChakramListener.this.thrownChakrams.remove(player.getUniqueId());
            this.cancel();
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private void applySpinAroundOwnAxis(ItemDisplay display, Vector direction, float angleDegrees) {
      if (!(direction.lengthSquared() <= 1.0E-4)) {
         Location facing = direction.toLocation(display.getWorld());
         display.setRotation(facing.getYaw(), facing.getPitch());
         Transformation trans = display.getTransformation();
         Quaternionf spin = new Quaternionf().rotateY((float)Math.toRadians(angleDegrees));
         trans.getLeftRotation().set(spin);
         display.setTransformation(trans);
      }
   }

   private void hitTarget(LivingEntity target, Player player) {
      double damage = this.plugin.getConfig().getDouble("weapons.freezing_chakram.throw_damage", 8.0);
      target.damage(damage, player);
      this.applyNativeFreeze(target);
      target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 2.0F);
      target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0.0, 1.0, 0.0), 25, 0.5, 0.5, 0.5, 0.1);
   }

   private boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.plugin.getConfig().getLong("weapons.freezing_chakram.cooldown_seconds", 8L) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         return System.currentTimeMillis() - lastUse < cooldownTime;
      }
   }

   private long getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getLong("weapons.freezing_chakram.cooldown_seconds", 8L) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   private void setCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      this.cooldowns.put(playerId, System.currentTimeMillis());
      Integer existingTask = this.cooldownEndTasks.remove(playerId);
      if (existingTask != null) {
         Bukkit.getScheduler().cancelTask(existingTask);
      }

      long cooldownSeconds = this.plugin.getConfig().getLong("weapons.freezing_chakram.cooldown_seconds", 8L);
      long cooldownTicks = Math.max(1L, cooldownSeconds * 20L);
      int taskId = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
         this.cooldownEndTasks.remove(playerId);
         Player onlinePlayer = Bukkit.getPlayer(playerId);
         if (onlinePlayer != null && onlinePlayer.isOnline()) {
            if (!this.thrownChakrams.containsKey(playerId)) {
               if (this.chakram.isFreezingChakram(onlinePlayer.getInventory().getItemInMainHand())) {
                  ItemStack offHand = onlinePlayer.getInventory().getItemInOffHand();
                  if (offHand == null || offHand.getType() == Material.AIR) {
                     this.equipOffhandChakram(onlinePlayer);
                  }
               }
            }
         }
      }, cooldownTicks).getTaskId();
      this.cooldownEndTasks.put(playerId, taskId);
   }

   private int getFreezeDurationTicks() {
      double seconds = this.plugin.getConfig().getDouble("weapons.freezing_chakram.freeze_duration_seconds", 5.0);
      return (int)Math.max(1L, Math.round(seconds * 20.0));
   }

   private void applyNativeFreeze(LivingEntity target) {
      final int freezeTicks = this.getFreezeDurationTicks();
      final int maxFreezeTicks = target.getMaxFreezeTicks();
      target.setFreezeTicks(Math.max(target.getFreezeTicks(), maxFreezeTicks));
      Integer existingTask = this.freezeHoldTasks.remove(target.getUniqueId());
      if (existingTask != null) {
         Bukkit.getScheduler().cancelTask(existingTask);
      }

      int taskId = (new BukkitRunnable() {
         private int lived = 0;

         public void run() {
            if (!target.isValid() || target.isDead()) {
               FreezingChakramListener.this.freezeHoldTasks.remove(target.getUniqueId());
               this.cancel();
            } else if (this.lived >= freezeTicks) {
               FreezingChakramListener.this.freezeHoldTasks.remove(target.getUniqueId());
               this.cancel();
            } else {
               target.setFreezeTicks(Math.max(target.getFreezeTicks(), maxFreezeTicks));
               this.lived++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L).getTaskId();
      this.freezeHoldTasks.put(target.getUniqueId(), taskId);
      target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, freezeTicks, 4, true, true));
      target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, freezeTicks, 2, true, true));
   }

   public long getRemainingCooldownSeconds(UUID playerId) {
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getLong("weapons.freezing_chakram.cooldown_seconds", 8L) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }
}
