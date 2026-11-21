package dev.cwhead.GravesX.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.ColorUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.World;
import org.bukkit.Vibration.Destination.BlockDestination;
import org.bukkit.Vibration.Destination.EntityDestination;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ParticleManager {
   private final Graves plugin;
   private final HashMap<UUID, Long> cooldowns;

   public ParticleManager(Graves plugin) {
      this.plugin = plugin;
      this.cooldowns = new HashMap<>();
   }

   public void startCompassParticleTrail(
      Location startLocation, Location endLocation, final Particle particleType, final int count, final double speed, long durationTicks, UUID playerUUID
   ) {
      if (startLocation != null && endLocation != null) {
         if (startLocation.getWorld() != null && endLocation.getWorld() != null) {
            if (startLocation.getWorld().equals(endLocation.getWorld())) {
               if (!(speed <= 0.0) && durationTicks > 0L) {
                  long now = System.currentTimeMillis();
                  long cooldownMs = Math.max(1L, durationTicks) * 50L;
                  Long last = this.cooldowns.putIfAbsent(playerUUID, now);
                  if (last == null || now - last >= cooldownMs) {
                     this.cooldowns.put(playerUUID, now);
                     final Location start = startLocation.clone().add(0.0, 2.0, 0.0);
                     final Location end = endLocation.clone().add(0.5, 0.3, 0.5);
                     final Vector dir = end.clone().subtract(start).toVector();
                     if (dir.lengthSquared() != 0.0) {
                        dir.normalize();
                        double totalDist = start.distance(end);
                        long maxStepsByDistance = (long)Math.ceil(totalDist / Math.max(1.0E-6, speed));
                        final long maxSteps = Math.min(durationTicks, Math.max(1L, maxStepsByDistance));

                        class TrailTask {
                           long step = 0L;
                           final Location cur = start.clone();

                           void scheduleNext() {
                              ParticleManager.this.plugin
                                 .getSchedulerManager()
                                 .runTaskLater(() -> ParticleManager.this.plugin.getSchedulerManager().execute(this.cur, this::tick), 1L);
                           }

                           void tick() {
                              try {
                                 if (this.step >= maxSteps) {
                                    return;
                                 }

                                 World w = this.cur.getWorld();
                                 if (w == null) {
                                    return;
                                 }

                                 w.spawnParticle(particleType, this.cur, count, 0.0, 0.0, 0.0, 0.0);
                                 this.cur.add(dir.clone().multiply(speed));
                                 this.step++;
                                 if (this.cur.distanceSquared(end) <= speed * speed) {
                                    return;
                                 }

                                 this.scheduleNext();
                              } catch (Throwable var2) {
                              }
                           }
                        }

                        new TrailTask().scheduleNext();
                     }
                  }
               }
            }
         }
      }
   }

   public BlockData getGravesBlockData(Grave grave, Location loc) {
      try {
         Method m = grave.getClass().getMethod("getBlockData");
         Object val = m.invoke(grave);
         if (val instanceof BlockData) {
            return (BlockData)val;
         }
      } catch (NoSuchMethodException var9) {
      } catch (Throwable var10) {
         this.plugin.debugMessage("getBlockData() reflect failed: " + var10.getMessage(), 2);
      }

      String mat = this.plugin.getConfigManager().getConfigSection("particle.block-material", grave).getString("particle.block-material", "STONE");
      String data = this.plugin.getConfigManager().getConfigSection("particle.block-data", grave).getString("particle.block-data", "");
      UUID gid = null;

      try {
         Method m = grave.getClass().getMethod("getUUID");
         Object v = m.invoke(grave);
         if (v instanceof UUID) {
            gid = (UUID)v;
         }
      } catch (Throwable var8) {
      }

      return new BlockData(loc, gid, mat, data);
   }

   public org.bukkit.block.data.BlockData toBukkitBlockData(BlockData gravesBD) {
      if (gravesBD == null) {
         return null;
      } else {
         String data = safe(gravesBD.getReplaceData());
         String matName = safe(gravesBD.getReplaceMaterial());

         try {
            if (!data.isEmpty()) {
               return Bukkit.createBlockData(data);
            }
         } catch (IllegalArgumentException var5) {
            this.plugin.debugMessage("Invalid replaceData for particle: " + data + " (using material fallback)", 1);
         }

         Material m = Material.matchMaterial(matName.isEmpty() ? "STONE" : matName);
         if (m == null || !m.isBlock()) {
            this.plugin.debugMessage("Invalid replaceMaterial for particle: " + matName + " (defaulting to STONE)", 1);
            m = Material.STONE;
         }

         return m.createBlockData();
      }
   }

   public ItemStack parseItemStack(Grave grave) {
      String matName = this.plugin.getConfigManager().getConfigSection("particle.item", grave).getString("particle.item", "STONE");
      Material m = Material.matchMaterial(matName);
      if (m == null || m.isAir()) {
         this.plugin.debugMessage("Invalid particle.item: " + matName + " (defaulting to STONE)", 1);
         m = Material.STONE;
      }

      return new ItemStack(m, 1);
   }

   public Color safeColor(String input, Color def) {
      Color c = ColorUtil.getColor(input);
      return c != null ? c : def;
   }

   public Vibration buildVibrationSingle(Graves plugin, Location graveLoc, Grave grave) {
      if (graveLoc != null && graveLoc.getWorld() != null) {
         World world = graveLoc.getWorld();
         String destMode = plugin.getConfigManager()
            .getConfigSection("particle.vibration.destination", grave)
            .getString("particle.vibration.destination", "block")
            .toLowerCase(Locale.ROOT);
         Player source = findSourcePlayer(plugin, grave, graveLoc, 5);
         if (source == null) {
            Location origin = center(graveLoc);
            Location dest = origin.clone().add(0.0, 0.001, 0.0);
            return new Vibration(origin, new BlockDestination(dest), 5);
         } else {
            Location origin = source.getLocation();
            Location graveCenter = center(graveLoc);
            if ("entity".equals(destMode)) {
               Player destEntity = pickRandomNearbyPlayer(world, graveCenter, 5);
               if (destEntity == null) {
                  destEntity = source;
               }

               return new Vibration(origin, new EntityDestination(destEntity), 5);
            } else {
               return new Vibration(origin, new BlockDestination(graveCenter), 5);
            }
         }
      } else {
         return null;
      }
   }

   public void spawnVibrationBounce(Graves plugin, Location graveLoc, Grave grave, Particle particle, int count) {
      if (graveLoc != null && graveLoc.getWorld() != null) {
         World world = graveLoc.getWorld();
         String destMode = plugin.getConfigManager()
            .getConfigSection("particle.vibration.destination", grave)
            .getString("particle.vibration.destination", "block")
            .toLowerCase(Locale.ROOT);
         Player source = findSourcePlayer(plugin, grave, graveLoc, 5);
         if (source == null) {
            Vibration vib = this.buildVibrationSingle(plugin, graveLoc, grave);
            world.spawnParticle(particle, graveLoc, count, vib);
         } else {
            Location originNow = source.getLocation();
            Location graveCenter = center(graveLoc);
            if ("entity".equals(destMode)) {
               Player destEntity = pickRandomNearbyPlayer(world, graveCenter, 5);
               if (destEntity == null) {
                  destEntity = source;
               }

               Vibration forward = new Vibration(originNow, new EntityDestination(destEntity), 5);
               world.spawnParticle(particle, originNow, count, forward);
               Player backFrom = destEntity;
               plugin.getSchedulerManager().runTaskLater(() -> {
                  Location backOrigin = backFrom.getLocation();
                  Vibration back = new Vibration(backOrigin, new EntityDestination(source), 5);
                  world.spawnParticle(particle, backOrigin, count, back);
               }, 5L);
            } else {
               Vibration forward = new Vibration(originNow, new BlockDestination(graveCenter), 5);
               world.spawnParticle(particle, originNow, count, forward);
               Location playerBlockCenter = center(originNow);
               plugin.getSchedulerManager().runTaskLater(() -> {
                  Vibration back = new Vibration(graveCenter, new BlockDestination(playerBlockCenter), 5);
                  world.spawnParticle(particle, graveCenter, count, back);
               }, 5L);
            }
         }
      }
   }

   public Object parseTrailData(Graves plugin, Grave grave) {
      int duration = plugin.getConfigManager().getConfigSection("particle.trail.duration-ticks", grave).getInt("particle.trail.duration-ticks", 20);
      double spread = plugin.getConfigManager().getConfigSection("particle.trail.spread", grave).getDouble("particle.trail.spread", 0.02);

      try {
         Class<?> trailClass = Class.forName("org.bukkit.Particle$Trail");

         for (Constructor<?> c : trailClass.getConstructors()) {
            Class<?>[] p = c.getParameterTypes();
            if (p.length == 2 && p[0] == int.class && p[1] == double.class) {
               return c.newInstance(duration, spread);
            }

            if (p.length == 1 && p[0] == int.class) {
               return c.newInstance(duration);
            }
         }

         return trailClass.getDeclaredConstructor().newInstance();
      } catch (Throwable var12) {
         plugin.debugMessage("TRAIL data type not available: " + var12.getMessage(), 2);
         return null;
      }
   }

   private static Player findSourcePlayer(Graves plugin, Grave grave, Location ref, int radius) {
      UUID owner = tryResolveOwnerUUID(plugin, grave);
      if (owner != null) {
         Player p = Bukkit.getPlayer(owner);
         if (p != null && p.isOnline() && sameWorld(p, ref)) {
            return p;
         }
      }

      double r2 = (double)radius * radius;
      Player best = null;
      double bestD2 = Double.MAX_VALUE;

      for (Player p : ref.getWorld().getPlayers()) {
         if (p.isOnline()) {
            double d2 = p.getLocation().distanceSquared(ref);
            if (d2 <= r2 && d2 < bestD2) {
               bestD2 = d2;
               best = p;
            }
         }
      }

      return best;
   }

   private static Player pickRandomNearbyPlayer(World world, Location center, int radius) {
      List<Player> candidates = world.getPlayers()
         .stream()
         .filter(OfflinePlayer::isOnline)
         .filter(p -> p.getLocation().distanceSquared(center) <= (double)radius * radius)
         .toList();
      return candidates.isEmpty() ? null : candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
   }

   private static UUID tryResolveOwnerUUID(Graves plugin, Grave grave) {
      String[] candidates = new String[]{"getPlayerUUID", "getOwnerUUID", "getOwnerId", "getOwner"};

      for (String name : candidates) {
         try {
            Method m = grave.getClass().getMethod(name);
            Object v = m.invoke(grave);
            if (v instanceof UUID) {
               return (UUID)v;
            }

            if (v instanceof OfflinePlayer) {
               return ((OfflinePlayer)v).getUniqueId();
            }

            if (v instanceof String) {
               return UUID.fromString((String)v);
            }
         } catch (NoSuchMethodException var9) {
         } catch (Throwable var10) {
            plugin.debugMessage("Owner UUID resolve failed via " + name + ": " + var10.getMessage(), 2);
         }
      }

      return null;
   }

   private static boolean sameWorld(Player p, Location ref) {
      return p.getWorld().equals(ref.getWorld());
   }

   private static Location center(Location l) {
      return new Location(l.getWorld(), l.getBlockX() + 0.5, l.getBlockY() + 0.5, l.getBlockZ() + 0.5);
   }

   private static String safe(String s) {
      return s == null ? "" : s.trim();
   }
}
