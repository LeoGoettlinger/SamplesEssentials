package de.theredend2000.advancedhunt.platform.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class PacketEventsParticleSender {
   private PacketEventsParticleSender() {
   }

   static boolean spawn(Player player, Location location, String particleName, int count, double offsetX, double offsetY, double offsetZ, double speed) {
      if (player == null || location == null) {
         return false;
      } else if (location.getWorld() == null) {
         return false;
      } else if (particleName == null || particleName.trim().isEmpty()) {
         return false;
      } else if (!isPacketEventsPluginEnabled()) {
         return false;
      } else if (!PacketEvents.getAPI().isInitialized()) {
         return false;
      } else {
         try {
            ParticleType<?> type = resolveParticleType(particleName);
            if (type == null) {
               return false;
            } else {
               Particle<?> particle = new Particle(type);
               Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());
               Vector3f offsets = new Vector3f((float)offsetX, (float)offsetY, (float)offsetZ);
               WrapperPlayServerParticle wrapper = new WrapperPlayServerParticle(particle, false, pos, offsets, (float)speed, count);
               PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);
               return true;
            }
         } catch (Throwable var17) {
            return false;
         }
      }
   }

   private static boolean isPacketEventsPluginEnabled() {
      try {
         Plugin plugin = Bukkit.getPluginManager().getPlugin("packetevents");
         if (plugin == null) {
            plugin = Bukkit.getPluginManager().getPlugin("PacketEvents");
         }

         return plugin != null && plugin.isEnabled();
      } catch (Throwable var1) {
         return false;
      }
   }

   private static ParticleType<?> resolveParticleType(String particleName) {
      String raw = particleName.trim();
      String lower = raw.toLowerCase(Locale.ROOT);
      ParticleType<?> type = ParticleTypes.getByName(raw);
      if (type != null) {
         return type;
      } else {
         type = ParticleTypes.getByName(lower);
         if (type != null) {
            return type;
         } else {
            type = ParticleTypes.getByName(lower.replace(' ', '_').replace('-', '_'));
            if (type != null) {
               return type;
            } else {
               if (lower.startsWith("minecraft:")) {
                  type = ParticleTypes.getByName(lower.substring("minecraft:".length()));
                  if (type != null) {
                     return type;
                  }
               } else {
                  type = ParticleTypes.getByName("minecraft:" + lower);
                  if (type != null) {
                     return type;
                  }
               }

               if ("villager_happy".equals(lower) || "VILLAGER_HAPPY".equals(raw)) {
                  type = ParticleTypes.getByName("happy_villager");
                  if (type != null) {
                     return type;
                  }
               }

               if ("villager_angry".equals(lower) || "VILLAGER_ANGRY".equals(raw)) {
                  type = ParticleTypes.getByName("angry_villager");
                  if (type != null) {
                     return type;
                  }
               }

               if ("smoke_normal".equals(lower) || "SMOKE_NORMAL".equals(raw)) {
                  type = ParticleTypes.getByName("smoke");
                  if (type != null) {
                     return type;
                  }
               }

               String maybeUpperSnake = raw.toUpperCase(Locale.ROOT);
               if (maybeUpperSnake.equals(raw) && raw.indexOf(95) >= 0) {
                  type = ParticleTypes.getByName(raw.toLowerCase(Locale.ROOT));
                  if (type != null) {
                     return type;
                  }
               }

               return null;
            }
         }
      }
   }
}
