package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import dev.demora.demoralegendaryweapons.weapons.mjolnir.MjolnirInputListener;
import java.util.UUID;
import org.bukkit.Input;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.util.Vector;

public final class ChargeDecayRunner {
   public static void run(RideStateCache state, DemoraLegendaryWeapons plugin) {
      long now = System.currentTimeMillis();

      for (Player player : plugin.getServer().getOnlinePlayers()) {
         UUID playerId = player.getUniqueId();
         Ravager mount = state.getMount(playerId);
         if (mount != null) {
            if (player.getVehicle() != mount) {
               state.removeRider(playerId);
            } else if (!mount.isDead() && mount.isValid()) {
               long durationSeconds = plugin.getConfig().getLong("weapons.ravager_horn.ride_duration_seconds", 30L);
               if (durationSeconds > 0L) {
                  Long start = state.getRideStart(playerId);
                  if (start != null && now - start >= durationSeconds * 1000L) {
                     player.leaveVehicle();
                     mount.remove();
                     state.removeRider(playerId);
                     state.startCooldown(playerId);
                     continue;
                  }
               }

               Object inputListenerObj = plugin.getMjolnirInputListener();
               if (inputListenerObj != null) {
                  Input input = ((MjolnirInputListener)inputListenerObj).getThrowChargeInput(playerId);
                  if (input == null) {
                     input = player.getCurrentInput();
                  }

                  if (input != null) {
                     float forward = (input.isForward() ? 1.0F : 0.0F) + (input.isBackward() ? -1.0F : 0.0F);
                     float sideways = (input.isLeft() ? 1.0F : 0.0F) + (input.isRight() ? -1.0F : 0.0F);
                     Vector dir = plugin.getVillagerStaff().prepareStaffDirection(player.getLocation());
                     if (dir != null) {
                        Vector rightDir = plugin.getDragonKatana().getDashLateral(dir);
                        if (rightDir != null) {
                           Vector finalMove = plugin.getExcalibur().resolveLungeDir(dir, forward, rightDir, sideways);
                           if (finalMove != null && finalMove.length() > 0.0) {
                              plugin.getReaperScythe().applyReapMultiplier(plugin, finalMove, input.isSprint(), input.isJump(), mount);
                              mount.setVelocity(finalMove);
                           }

                           mount.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());
                           tryAutoAttack(mount, player, now, state, plugin);
                        }
                     }
                  }
               }
            } else {
               state.removeRider(playerId);
            }
         }
      }
   }

   private static void tryAutoAttack(Ravager ravager, Player rider, long now, RideStateCache state, DemoraLegendaryWeapons plugin) {
      double radius = plugin.getConfig().getDouble("weapons.ravager_horn.auto_attack_radius", 2.5);
      long cooldownMs = plugin.getConfig().getLong("weapons.ravager_horn.auto_attack_cooldown_ms", 1000L);
      Long last = state.getLastHitAt(ravager.getUniqueId());
      if (last == null || now - last >= cooldownMs) {
         Entity bestTarget = null;
         double bestDistSq = Double.MAX_VALUE;
         Vector look = rider.getLocation().getDirection().setY(0);
         if (!(look.lengthSquared() < 1.0E-6)) {
            look.normalize();

            for (Entity entity : ravager.getNearbyEntities(radius, radius, radius)) {
               if (entity instanceof Player target && !ravager.getPassengers().contains(target)) {
                  Vector to = target.getLocation().toVector().subtract(ravager.getLocation().toVector());
                  to.setY(0);
                  if (!(to.lengthSquared() < 1.0E-6)) {
                     to.normalize();
                     if (!(look.dot(to) < 0.4)) {
                        double distSq = target.getLocation().distanceSquared(ravager.getLocation());
                        if (distSq < bestDistSq) {
                           bestDistSq = distSq;
                           bestTarget = target;
                        }
                     }
                  }
               }
            }

            if (bestTarget instanceof LivingEntity living) {
               state.recordHit(ravager.getUniqueId(), now);
               ravager.attack(living);
            }
         }
      }
   }
}
