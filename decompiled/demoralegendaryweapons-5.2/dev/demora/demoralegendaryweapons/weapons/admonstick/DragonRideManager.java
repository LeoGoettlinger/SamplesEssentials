package dev.demora.demoralegendaryweapons.weapons.admonstick;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.entity.TeleportFlag.EntityState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DragonRideManager {
   private final DemoraLegendaryWeapons plugin;
   private final Map<UUID, UUID> dragonByPlayer = new HashMap<>();

   public DragonRideManager(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.startControlLoop();
   }

   public void summon(Player player) {
      if (player.getVehicle() == null) {
         if (!this.dragonByPlayer.containsKey(player.getUniqueId())) {
            Location spawnLoc = player.getLocation().clone().add(0.0, 0.5, 0.0);
            EnderDragon dragon = (EnderDragon)player.getWorld().spawn(spawnLoc, EnderDragon.class, spawned -> {
               spawned.setPhase(Phase.HOVER);
               spawned.setAI(false);
               spawned.setGravity(false);
               spawned.setInvulnerable(true);
               spawned.setPersistent(false);
               spawned.setRemoveWhenFarAway(false);
            });
            this.dragonByPlayer.put(player.getUniqueId(), dragon.getUniqueId());
            dragon.addPassenger(player);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2F, 1.0F);
         }
      }
   }

   public void handleDismount(Entity passenger, Entity vehicle) {
      if (passenger instanceof Player player) {
         UUID dragonId = this.dragonByPlayer.get(player.getUniqueId());
         if (dragonId != null) {
            if (vehicle != null && vehicle.getUniqueId().equals(dragonId)) {
               this.cleanup(player.getUniqueId());
            }
         }
      }
   }

   public void handleQuit(Player player) {
      this.cleanup(player.getUniqueId());
   }

   private void cleanup(UUID playerId) {
      UUID dragonId = this.dragonByPlayer.remove(playerId);
      if (dragonId != null) {
         Entity dragon = Bukkit.getEntity(dragonId);
         if (dragon != null) {
            dragon.getWorld().playSound(dragon.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.7F, 1.4F);
            dragon.remove();
         }
      }
   }

   private void startControlLoop() {
      (new BukkitRunnable() {
         public void run() {
            if (!DragonRideManager.this.dragonByPlayer.isEmpty()) {
               for (UUID playerId : new ArrayList<>(DragonRideManager.this.dragonByPlayer.keySet())) {
                  Player player = Bukkit.getPlayer(playerId);
                  if (player != null && player.isOnline()) {
                     UUID dragonId = DragonRideManager.this.dragonByPlayer.get(playerId);
                     if (!((dragonId != null ? Bukkit.getEntity(dragonId) : null) instanceof EnderDragon dragon && !dragon.isDead() && dragon.isValid())) {
                        DragonRideManager.this.cleanup(playerId);
                     } else if (player.getVehicle() != dragon) {
                        DragonRideManager.this.cleanup(playerId);
                     } else {
                        DragonRideManager.this.controlDragon(player, dragon);
                     }
                  } else {
                     DragonRideManager.this.cleanup(playerId);
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 1L, 1L);
   }

   private void controlDragon(Player player, EnderDragon dragon) {
      Input input = player.getCurrentInput();
      Location base = dragon.getLocation();
      float yaw = player.getLocation().getYaw();
      float pitch = player.getLocation().getPitch();
      Vector move = new Vector(0, 0, 0);
      if (input != null) {
         Vector look = player.getLocation().getDirection();
         Vector flat = new Vector(look.getX(), 0.0, look.getZ());
         if (flat.lengthSquared() < 1.0E-6) {
            flat = new Vector(0, 0, 1);
         } else {
            flat.normalize();
         }

         Vector rightVector = new Vector(flat.getZ(), 0.0, -flat.getX());
         double forward = (input.isForward() ? 1.0 : 0.0) - (input.isBackward() ? 1.0 : 0.0);
         double strafe = (input.isRight() ? 1.0 : 0.0) - (input.isLeft() ? 1.0 : 0.0);
         if (forward != 0.0) {
            move.add(look.clone().multiply(forward));
         }

         if (strafe != 0.0) {
            move.add(rightVector.clone().multiply(strafe));
         }

         if (input.isJump()) {
            move.add(new Vector(0.0, 1.0, 0.0));
         }

         double speed = input.isSprint() ? 1.3 : 0.75;
         if (move.lengthSquared() > 1.0E-6) {
            move.normalize().multiply(speed);
         } else {
            move = new Vector(0, 0, 0);
         }
      }

      Location target = base.clone().add(move);
      target.setYaw(yaw);
      target.setPitch(pitch);
      dragon.teleport(target, new TeleportFlag[]{EntityState.RETAIN_PASSENGERS});
   }
}
