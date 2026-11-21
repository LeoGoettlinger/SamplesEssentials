package de.theredend2000.advancedhunt.platform.impl;

import de.theredend2000.advancedhunt.lib.xseries.particles.XParticle;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

final class ModernParticleSpawner {
   private static final Map<String, Particle> PARTICLE_CACHE = new ConcurrentHashMap<>();

   void spawn(Location location, String particleName, int count, double offsetX, double offsetY, double offsetZ, double speed) {
      if (location != null) {
         World world = location.getWorld();
         if (world != null) {
            Particle particle = resolveParticle(particleName);
            if (particle != null) {
               try {
                  world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
               } catch (Throwable var15) {
               }
            }
         }
      }
   }

   void spawnForPlayer(Player player, Location location, String particleName, int count, double offsetX, double offsetY, double offsetZ, double speed) {
      if (player != null && location != null) {
         if (location.getWorld() != null) {
            Particle particle = resolveParticle(particleName);
            if (particle != null) {
               try {
                  player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
               } catch (Throwable var15) {
               }
            }
         }
      }
   }

   private static Particle resolveParticle(String particleName) {
      if (particleName == null) {
         return null;
      } else {
         String upper = particleName.trim().toUpperCase();
         if (upper.isEmpty()) {
            return null;
         } else {
            Particle cached = PARTICLE_CACHE.get(upper);
            if (cached != null) {
               return cached;
            } else {
               Optional<XParticle> xParticle = XParticle.of(upper);
               Particle particle = xParticle.<Particle>map(XParticle::get).orElse(null);
               if (particle == null) {
                  return null;
               } else {
                  PARTICLE_CACHE.put(upper, particle);
                  return particle;
               }
            }
         }
      }
   }
}
