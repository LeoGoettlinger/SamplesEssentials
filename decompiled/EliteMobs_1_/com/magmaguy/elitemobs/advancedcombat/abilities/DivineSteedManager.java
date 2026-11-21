package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

final class DivineSteedManager implements Listener, AutoCloseable {
   private static final double MAXIMUM_HEALTH = 30.0;
   private static final double MOVEMENT_SPEED = 0.24;
   private static final double JUMP_STRENGTH = 0.72;
   private static final int LANDING_GRACE_TICKS = 20;
   private final Map<UUID, DivineSteedManager.Ride> ridesByOwner = new LinkedHashMap<>();
   private final Map<UUID, DivineSteedManager.Ride> ridesBySteed = new LinkedHashMap<>();
   private final Attribute maximumHealthAttribute;
   private final Attribute movementSpeedAttribute;
   private final Attribute jumpStrengthAttribute;
   private final Logger logger;
   private final BukkitTask maintenanceTask;
   private boolean closed;

   DivineSteedManager(Plugin plugin) {
      Plugin owner = Objects.requireNonNull(plugin, "plugin");
      this.logger = owner.getLogger();
      Registry<Attribute> attributes = owner.getServer().getRegistry(Attribute.class);
      this.maximumHealthAttribute = attribute(attributes, "max_health");
      this.movementSpeedAttribute = attribute(attributes, "movement_speed");
      this.jumpStrengthAttribute = attribute(attributes, "jump_strength");
      owner.getServer().getPluginManager().registerEvents(this, owner);
      this.maintenanceTask = owner.getServer().getScheduler().runTaskTimer(owner, this::tick, 1L, 1L);
   }

   Optional<Horse> summon(Player player, int durationTicks, DivineSteedManager.RideListener listener) {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(listener, "listener");
      if (!this.closed && durationTicks >= 1 && player.isOnline() && player.isValid() && !player.isDead()) {
         DivineSteedManager.Ride existing = this.ridesByOwner.get(player.getUniqueId());
         Entity vehicle = player.getVehicle();
         if (vehicle == null || existing != null && vehicle.getUniqueId().equals(existing.steed().getUniqueId())) {
            if (existing != null) {
               this.terminate(existing, true);
            }

            Horse horse = null;

            try {
               horse = (Horse)player.getWorld().spawn(player.getLocation(), Horse.class);
               if (horse == null) {
                  return Optional.empty();
               } else {
                  this.configure(player, horse);
                  if (horse.addPassenger(player) && horse.getPassengers().contains(player)) {
                     DivineSteedManager.Ride ride = new DivineSteedManager.Ride(player, horse, durationTicks, listener);
                     this.ridesByOwner.put(player.getUniqueId(), ride);
                     this.ridesBySteed.put(horse.getUniqueId(), ride);
                     return Optional.of(horse);
                  } else {
                     this.discard(horse);
                     return Optional.empty();
                  }
               }
            } catch (RuntimeException var8) {
               if (horse != null) {
                  this.discard(horse);
               }

               return Optional.empty();
            }
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   void deactivate(Player player) {
      Objects.requireNonNull(player, "player");
      DivineSteedManager.Ride ride = this.ridesByOwner.get(player.getUniqueId());
      if (ride != null) {
         this.terminate(ride, true);
      }
   }

   private void configure(Player player, Horse horse) {
      horse.setPersistent(false);
      horse.setRemoveWhenFarAway(true);
      horse.setAdult();
      horse.setAgeLock(true);
      horse.setTamed(true);
      horse.setOwner(player);
      horse.setDomestication(horse.getMaxDomestication());
      horse.setColor(Color.WHITE);
      horse.setStyle(Style.NONE);
      horse.setCustomName("Divine Steed");
      horse.setCustomNameVisible(false);
      horse.setCanPickupItems(false);
      horse.setLootTable(null);
      horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
      horse.getInventory().setArmor(new ItemStack(Material.GOLDEN_HORSE_ARMOR));
      setBaseAttribute(horse, this.maximumHealthAttribute, 30.0);
      setBaseAttribute(horse, this.movementSpeedAttribute, 0.24);
      if (this.jumpStrengthAttribute == null) {
         horse.setJumpStrength(0.72);
      } else {
         setBaseAttribute(horse, this.jumpStrengthAttribute, 0.72);
      }

      if (this.maximumHealthAttribute != null) {
         horse.setHealth(30.0);
      }
   }

   private static Attribute attribute(Registry<Attribute> registry, String key) {
      return registry == null ? null : (Attribute)registry.get(NamespacedKey.minecraft(key));
   }

   private static void setBaseAttribute(Horse horse, Attribute attribute, double value) {
      if (attribute != null) {
         AttributeInstance instance = horse.getAttribute(attribute);
         if (instance != null) {
            instance.setBaseValue(value);
         }
      }
   }

   private void tick() {
      for (DivineSteedManager.Ride ride : List.copyOf(this.ridesByOwner.values())) {
         if (!active(ride)) {
            this.terminate(ride, true);
         } else {
            if (ride.remainingTicks() > 0) {
               try {
                  ride.listener().onTick(ride.steed());
               } catch (RuntimeException var4) {
                  this.terminate(ride, true);
                  continue;
               }

               ride.elapseTick();
            }

            if (ride.remainingTicks() <= 0) {
               ride.elapseLandingGrace();
               if (ride.steed().isOnGround() || ride.landingGraceTicks() >= 20) {
                  this.terminate(ride, true);
               }
            }
         }
      }
   }

   private static boolean active(DivineSteedManager.Ride ride) {
      Player owner = ride.owner();
      Horse steed = ride.steed();
      Entity vehicle = owner.getVehicle();
      return owner.isOnline()
         && owner.isValid()
         && !owner.isDead()
         && steed.isValid()
         && !steed.isDead()
         && owner.getWorld().equals(steed.getWorld())
         && vehicle != null
         && vehicle.getUniqueId().equals(steed.getUniqueId())
         && steed.getPassengers().stream().anyMatch(passenger -> passenger.getUniqueId().equals(owner.getUniqueId()));
   }

   private void terminate(DivineSteedManager.Ride ride, boolean removeHorse) {
      if (this.ridesByOwner.remove(ride.owner().getUniqueId(), ride)) {
         this.ridesBySteed.remove(ride.steed().getUniqueId(), ride);

         try {
            ride.listener().onEnd(ride.steed());
         } catch (RuntimeException var4) {
            this.warn("Divine Steed end callback failed", var4);
         }

         if (removeHorse) {
            this.discard(ride.steed());
         }
      }
   }

   private void discard(Horse horse) {
      this.attempt("clear temporary equipment", () -> clearEquipment(horse));
      this.attempt("eject riders", horse::eject);
      this.attempt("remove temporary horse", () -> {
         if (horse.isValid()) {
            horse.remove();
         }
      });
   }

   private static void clearEquipment(Horse horse) {
      horse.getInventory().setSaddle(new ItemStack(Material.AIR));
      horse.getInventory().setArmor(new ItemStack(Material.AIR));
   }

   private void attempt(String operation, Runnable action) {
      try {
         action.run();
      } catch (RuntimeException var4) {
         this.warn("Could not " + operation, var4);
      }
   }

   private void warn(String message, RuntimeException exception) {
      if (this.logger != null) {
         this.logger.log(Level.WARNING, message, (Throwable)exception);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onDeath(EntityDeathEvent event) {
      DivineSteedManager.Ride ride = this.ridesBySteed.get(event.getEntity().getUniqueId());
      if (ride != null) {
         event.getDrops().clear();
         event.setDroppedExp(0);
         this.terminate(ride, false);
         this.attempt("clear dead steed equipment", () -> clearEquipment(ride.steed()));
         this.attempt("eject riders from dead steed", ride.steed()::eject);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityRemoved(EntityRemoveEvent event) {
      DivineSteedManager.Ride ride = this.ridesBySteed.get(event.getEntity().getUniqueId());
      if (ride != null) {
         this.terminate(ride, false);
         this.attempt("clear removed steed equipment", () -> clearEquipment(ride.steed()));
         this.attempt("eject riders from removed steed", ride.steed()::eject);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getInventory().getHolder() instanceof Horse horse && this.ridesBySteed.containsKey(horse.getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onVehicleExit(VehicleExitEvent event) {
      DivineSteedManager.Ride ride = this.ridesBySteed.get(event.getVehicle().getUniqueId());
      if (ride != null && event.getExited().getUniqueId().equals(ride.owner().getUniqueId())) {
         this.terminate(ride, true);
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.attempt("cancel steed maintenance", this.maintenanceTask::cancel);

         for (DivineSteedManager.Ride ride : List.copyOf(this.ridesByOwner.values())) {
            this.terminate(ride, true);
         }

         this.ridesByOwner.clear();
         this.ridesBySteed.clear();
         HandlerList.unregisterAll(this);
      }
   }

   private static final class Ride {
      private final Player owner;
      private final Horse steed;
      private final DivineSteedManager.RideListener listener;
      private int remainingTicks;
      private int landingGraceTicks;

      private Ride(Player owner, Horse steed, int remainingTicks, DivineSteedManager.RideListener listener) {
         this.owner = owner;
         this.steed = steed;
         this.remainingTicks = remainingTicks;
         this.listener = listener;
      }

      Player owner() {
         return this.owner;
      }

      Horse steed() {
         return this.steed;
      }

      DivineSteedManager.RideListener listener() {
         return this.listener;
      }

      int remainingTicks() {
         return this.remainingTicks;
      }

      void elapseTick() {
         this.remainingTicks--;
      }

      int landingGraceTicks() {
         return this.landingGraceTicks;
      }

      void elapseLandingGrace() {
         this.landingGraceTicks++;
      }
   }

   interface RideListener {
      DivineSteedManager.RideListener NOOP = horse -> {};

      void onTick(Horse var1);

      default void onEnd(Horse horse) {
      }
   }
}
