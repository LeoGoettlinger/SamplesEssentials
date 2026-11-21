package com.magmaguy.elitemobs.magmacore.scripting.tables;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.location.LocationQueryRegistry;
import com.magmaguy.elitemobs.magmacore.util.TemporaryBlockManager;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpactHandle;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpactRequest;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpacts;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class LuaWorldTable {
   private static final List<BiConsumer<LuaTable, World>> enrichers = new CopyOnWriteArrayList<>();

   private LuaWorldTable() {
   }

   public static void registerEnricher(BiConsumer<LuaTable, World> enricher) {
      if (enricher != null) {
         enrichers.add(enricher);
      }
   }

   public static void playSound(Location location, String soundName, float volume, float pitch) {
      if (location != null && location.getWorld() != null && soundName != null) {
         String candidate = soundName.trim();
         if (!candidate.isEmpty()) {
            Sound sound = resolveRegisteredSound(candidate);
            if (sound != null) {
               location.getWorld().playSound(location, sound, volume, pitch);
            } else {
               String customSoundKey = normalizeCustomSoundKey(candidate);
               if (customSoundKey != null) {
                  location.getWorld().playSound(location, customSoundKey, volume, pitch);
               }
            }
         }
      }
   }

   public static Sound resolveRegisteredSound(String soundName) {
      if (soundName != null && !soundName.isBlank()) {
         try {
            return Sound.valueOf(soundName.trim().toUpperCase(Locale.ROOT));
         } catch (RuntimeException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   static String normalizeCustomSoundKey(String soundName) {
      if (soundName == null) {
         return null;
      } else {
         String normalized = soundName.trim().toLowerCase(Locale.ROOT);
         if (normalized.isEmpty()) {
            return null;
         } else {
            try {
               NamespacedKey key = NamespacedKey.fromString(normalized);
               return key == null ? null : key.toString();
            } catch (RuntimeException var3) {
               return null;
            }
         }
      }
   }

   public static LuaTable build(World world) {
      LuaTable table = new LuaTable();
      if (world == null) {
         return table;
      } else {
         table.set("get_entity", method(table, args -> {
            Entity entity = Bukkit.getEntity(UUID.fromString(args.checkjstring(1)));
            if (entity != null && entity.isValid() && entity.getWorld().equals(world)) {
               return entity instanceof LivingEntity living ? LuaLivingEntityTable.build(living) : LuaEntityTable.build(entity);
            } else {
               return LuaValue.NIL;
            }
         }));
         table.set(
            "block_is_passable",
            method(
               table,
               args -> {
                  int x = args.checkint(1);
                  int y = args.checkint(2);
                  int z = args.checkint(3);
                  return (Varargs)(world.isChunkLoaded(x >> 4, z >> 4) && y >= world.getMinHeight() && y < world.getMaxHeight()
                     ? LuaValue.valueOf(world.getBlockAt(x, y, z).isPassable())
                     : LuaValue.NIL);
               }
            )
         );
         table.set(
            "location_is_protected",
            method(table, args -> LuaValue.valueOf(LocationQueryRegistry.isInAnyProtectedRegion(LuaTableSupport.tableToLocation(args.checktable(1), world))))
         );
         table.set(
            "spawn_dust",
            method(
               table,
               args -> {
                  Location at = LuaTableSupport.tableToLocation(args.checktable(1), world);
                  if (!world.isChunkLoaded(at.getBlockX() >> 4, at.getBlockZ() >> 4)) {
                     return LuaValue.NIL;
                  } else {
                     world.spawnParticle(
                        Particle.DUST,
                        at,
                        args.checkint(2),
                        0.1,
                        0.1,
                        0.1,
                        1.0,
                        new DustOptions(Color.fromRGB(args.checkint(3), args.checkint(4), args.checkint(5)), 1.0F)
                     );
                     return LuaValue.NIL;
                  }
               }
            )
         );
         table.set("name", world.getName());
         table.set(
            "get_block_at",
            method(
               table,
               args -> {
                  int x = args.checkint(1);
                  int y = args.checkint(2);
                  int z = args.checkint(3);
                  return !world.isChunkLoaded(x >> 4, z >> 4)
                     ? LuaValue.valueOf("air")
                     : LuaValue.valueOf(world.getBlockAt(x, y, z).getType().name().toLowerCase(Locale.ROOT));
               }
            )
         );
         table.set("spawn_particle", method(table, args -> {
            String particleName = args.checkjstring(1).toUpperCase(Locale.ROOT);

            Particle particle;
            try {
               particle = Particle.valueOf(particleName);
            } catch (IllegalArgumentException var19) {
               return LuaValue.NIL;
            }

            double x = args.checkdouble(2);
            double y = args.checkdouble(3);
            double z = args.checkdouble(4);
            int count = args.optint(5, 1);
            double dx = args.optdouble(6, 0.0);
            double dy = args.optdouble(7, 0.0);
            double dz = args.optdouble(8, 0.0);
            double speed = args.optdouble(9, 0.0);
            world.spawnParticle(particle, x, y, z, count, dx, dy, dz, speed);
            return LuaValue.NIL;
         }));
         table.set("get_time", method(table, args -> LuaValue.valueOf((double)world.getTime())));
         table.set("set_time", method(table, args -> {
            world.setTime(args.checklong(1));
            return LuaValue.NIL;
         }));
         table.set("play_sound", method(table, args -> {
            double x = args.checkdouble(2);
            double y = args.checkdouble(3);
            double z = args.checkdouble(4);
            float volume = (float)args.optdouble(5, 1.0);
            float pitch = (float)args.optdouble(6, 1.0);
            playSound(new Location(world, x, y, z), args.checkjstring(1), volume, pitch);
            return LuaValue.NIL;
         }));
         table.set("strike_lightning", method(table, args -> {
            double x = args.checkdouble(1);
            double y = args.checkdouble(2);
            double z = args.checkdouble(3);
            world.strikeLightning(new Location(world, x, y, z));
            return LuaValue.NIL;
         }));
         table.set("get_nearby_entities", method(table, args -> {
            double x = args.checkdouble(1);
            double y = args.checkdouble(2);
            double z = args.checkdouble(3);
            double radius = args.checkdouble(4);
            Location center = new Location(world, x, y, z);
            Collection<Entity> nearby = world.getNearbyEntities(center, radius, radius, radius);
            LuaTable result = new LuaTable();
            int index = 1;

            for (Entity entity : nearby) {
               if (entity instanceof LivingEntity livingEntity) {
                  result.set(index++, LuaLivingEntityTable.build(livingEntity));
               } else {
                  result.set(index++, LuaEntityTable.build(entity));
               }
            }

            return result;
         }));
         table.set("spawn_entity", method(table, args -> {
            String entityTypeName = args.checkjstring(1).toUpperCase(Locale.ROOT);

            EntityType entityType;
            try {
               entityType = EntityType.valueOf(entityTypeName);
            } catch (IllegalArgumentException var12) {
               return LuaValue.NIL;
            }

            double x = args.checkdouble(2);
            double y = args.checkdouble(3);
            double z = args.checkdouble(4);
            Entity spawned = world.spawnEntity(new Location(world, x, y, z), entityType);
            return spawned instanceof LivingEntity livingEntity ? LuaLivingEntityTable.build(livingEntity) : LuaEntityTable.build(spawned);
         }));
         table.set("set_block_at", method(table, args -> {
            int x = args.checkint(1);
            int y = args.checkint(2);
            int z = args.checkint(3);
            String materialName = args.checkjstring(4);
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
               return LuaValue.FALSE;
            } else {
               Bukkit.getScheduler().runTask(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                  if (world.isChunkLoaded(x >> 4, z >> 4)) {
                     world.getBlockAt(x, y, z).setType(material);
                  }
               });
               return LuaValue.TRUE;
            }
         }));
         table.set("get_highest_block_y", method(table, args -> {
            int x = args.checkint(1);
            int z = args.checkint(2);
            return LuaValue.valueOf(world.getHighestBlockYAt(x, z));
         }));
         table.set("get_nearby_players", method(table, args -> {
            double x = args.checkdouble(1);
            double y = args.checkdouble(2);
            double z = args.checkdouble(3);
            double radius = args.checkdouble(4);
            Location center = new Location(world, x, y, z);
            Collection<Entity> nearby = world.getNearbyEntities(center, radius, radius, radius);
            LuaTable result = new LuaTable();
            int index = 1;

            for (Entity entity : nearby) {
               if (entity instanceof Player) {
                  result.set(index++, LuaLivingEntityTable.build((LivingEntity)entity));
               }
            }

            return result;
         }));
         table.set(
            "raycast",
            method(
               table,
               args -> {
                  double fx = args.checkdouble(1);
                  double fy = args.checkdouble(2);
                  double fz = args.checkdouble(3);
                  double dx = args.checkdouble(4);
                  double dy = args.checkdouble(5);
                  double dz = args.checkdouble(6);
                  double maxDist = args.optdouble(7, 50.0);
                  FluidCollisionMode fluid = FluidCollisionMode.valueOf(args.optjstring(8, "NEVER").toUpperCase(Locale.ROOT));
                  boolean blocksOnly = args.optboolean(9, false);
                  Location start = new Location(world, fx, fy, fz);
                  Vector direction = new Vector(dx, dy, dz).normalize();
                  if (args.optboolean(10, false)) {
                     if (!Double.isFinite(maxDist) || maxDist <= 0.0 || !Double.isFinite(direction.lengthSquared())) {
                        return LuaValue.NIL;
                     }

                     Location end = start.clone().add(direction.clone().multiply(maxDist));
                     int minX = Math.min(start.getBlockX(), end.getBlockX()) >> 4;
                     int maxX = Math.max(start.getBlockX(), end.getBlockX()) >> 4;
                     int minZ = Math.min(start.getBlockZ(), end.getBlockZ()) >> 4;
                     int maxZ = Math.max(start.getBlockZ(), end.getBlockZ()) >> 4;

                     for (int x = minX; x <= maxX; x++) {
                        for (int z = minZ; z <= maxZ; z++) {
                           if (!world.isChunkLoaded(x, z)) {
                              return LuaValue.NIL;
                           }
                        }
                     }
                  }

                  RayTraceResult result = blocksOnly
                     ? world.rayTraceBlocks(start, direction, maxDist, fluid)
                     : world.rayTrace(start, direction, maxDist, fluid, fluid == FluidCollisionMode.NEVER, 0.5, null);
                  LuaTable resultTable = new LuaTable();
                  if (result == null) {
                     resultTable.set("hit_entity", LuaValue.NIL);
                     resultTable.set("hit_location", LuaValue.NIL);
                     resultTable.set("hit_block", LuaValue.NIL);
                     return resultTable;
                  } else {
                     if (result.getHitEntity() != null) {
                        Entity hitEntity = result.getHitEntity();
                        if (hitEntity instanceof LivingEntity le) {
                           resultTable.set("hit_entity", LuaLivingEntityTable.build(le));
                        } else {
                           resultTable.set("hit_entity", LuaEntityTable.build(hitEntity));
                        }
                     } else {
                        resultTable.set("hit_entity", LuaValue.NIL);
                     }

                     if (result.getHitPosition() != null) {
                        Vector hitPos = result.getHitPosition();
                        resultTable.set("hit_location", LuaTableSupport.locationToTable(new Location(world, hitPos.getX(), hitPos.getY(), hitPos.getZ())));
                     }

                     if (result.getHitBlock() != null) {
                        Block hitBlock = result.getHitBlock();
                        LuaTable blockTable = new LuaTable();
                        blockTable.set("x", hitBlock.getX());
                        blockTable.set("y", hitBlock.getY());
                        blockTable.set("z", hitBlock.getZ());
                        blockTable.set("material", hitBlock.getType().name().toLowerCase(Locale.ROOT));
                        resultTable.set("hit_block", blockTable);
                     } else {
                        resultTable.set("hit_block", LuaValue.NIL);
                     }

                     return resultTable;
                  }
               }
            )
         );
         table.set("place_temporary_block", method(table, args -> {
            int x = args.checkint(1);
            int y = args.checkint(2);
            int z = args.checkint(3);
            String materialName = args.checkjstring(4);
            int ticks = args.optint(5, 0);
            boolean requireAir = args.optboolean(6, false);
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
               return LuaValue.FALSE;
            } else if (!world.isChunkLoaded(x >> 4, z >> 4)) {
               return LuaValue.FALSE;
            } else {
               Block block = world.getBlockAt(x, y, z);
               TemporaryBlockManager.addTemporaryBlock(block, ticks, material, requireAir);
               return LuaValue.TRUE;
            }
         }));
         table.set("drop_item", method(table, args -> {
            double x = args.checkdouble(1);
            double y = args.checkdouble(2);
            double z = args.checkdouble(3);
            String materialName = args.checkjstring(4);
            int amount = args.optint(5, 1);
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
               return LuaValue.NIL;
            } else {
               Location loc = new Location(world, x, y, z);
               Item dropped = world.dropItemNaturally(loc, new ItemStack(material, amount));
               return LuaEntityTable.build(dropped);
            }
         }));
         table.set("spawn_firework", method(table, args -> {
            double x = args.checkdouble(1);
            double y = args.checkdouble(2);
            double z = args.checkdouble(3);
            LuaTable colorsTable = args.checktable(4);
            String typeName = args.optjstring(5, "BALL");
            int power = args.optint(6, 1);
            Location loc = new Location(world, x, y, z);
            Bukkit.getScheduler().runTask(MagmaCore.getInstance().getRequestingPlugin(), () -> {
               Firework firework = (Firework)world.spawnEntity(loc, EntityType.FIREWORK_ROCKET);
               FireworkMeta meta = firework.getFireworkMeta();
               List<Color> colors = new ArrayList<>();

               for (int i = 1; i <= colorsTable.length(); i++) {
                  String colorName = colorsTable.get(i).tojstring().toUpperCase(Locale.ROOT);

                  try {
                     Field field = Color.class.getField(colorName);
                     colors.add((Color)field.get(null));
                  } catch (Exception var12) {
                  }
               }

               if (colors.isEmpty()) {
                  colors.add(Color.WHITE);
               }

               Type effectType;
               try {
                  effectType = Type.valueOf(typeName.toUpperCase(Locale.ROOT));
               } catch (Exception var11x) {
                  effectType = Type.BALL;
               }

               FireworkEffect effect = FireworkEffect.builder().withColor(colors).with(effectType).trail(true).build();
               meta.addEffect(effect);
               meta.setPower(power);
               firework.setFireworkMeta(meta);
            });
            return LuaValue.NIL;
         }));
         table.set("strike_lightning_at_location", method(table, args -> {
            Location loc = locArg(args, world);
            if (loc != null && loc.getWorld() != null) {
               loc.getWorld().strikeLightning(loc);
            }

            return LuaValue.NIL;
         }));
         table.set("play_sound_at_location", method(table, args -> {
            Location loc = locArg(args, world);
            if (loc == null) {
               return LuaValue.NIL;
            } else {
               playSound(loc, args.checkjstring(2), (float)args.optdouble(3, 1.0), (float)args.optdouble(4, 1.0));
               return LuaValue.NIL;
            }
         }));
         table.set("spawn_particle_at_location", method(table, args -> {
            Location loc = locArg(args, world);
            if (loc == null) {
               return LuaValue.NIL;
            } else {
               Particle particle;
               try {
                  particle = Particle.valueOf(args.checkjstring(2).toUpperCase(Locale.ROOT));
               } catch (IllegalArgumentException var5x) {
                  return LuaValue.NIL;
               }

               int count = args.optint(3, 1);
               world.spawnParticle(particle, loc, count, args.optdouble(4, 0.0), args.optdouble(5, 0.0), args.optdouble(6, 0.0), args.optdouble(7, 0.0));
               return LuaValue.NIL;
            }
         }));
         table.set(
            "get_block_at_location",
            method(
               table,
               args -> {
                  Location loc = locArg(args, world);
                  return loc != null && loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)
                     ? LuaValue.valueOf(loc.getBlock().getType().name().toLowerCase(Locale.ROOT))
                     : LuaValue.valueOf("air");
               }
            )
         );
         table.set("set_block_at_location", method(table, args -> {
            Location loc = locArg(args, world);
            Material material = Material.matchMaterial(args.checkjstring(2));
            if (loc != null && loc.getWorld() != null && material != null) {
               Bukkit.getScheduler().runTask(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                  if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                     loc.getBlock().setType(material);
                  }
               });
               return LuaValue.TRUE;
            } else {
               return LuaValue.FALSE;
            }
         }));
         table.set("place_temporary_block_at_location", method(table, args -> {
            Location loc = locArg(args, world);
            Material material = Material.matchMaterial(args.checkjstring(2));
            if (loc != null && loc.getWorld() != null && material != null) {
               if (!loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                  return LuaValue.FALSE;
               } else {
                  TemporaryBlockManager.addTemporaryBlock(loc.getBlock(), args.optint(3, 0), material, args.optboolean(4, false));
                  return LuaValue.TRUE;
               }
            } else {
               return LuaValue.FALSE;
            }
         }));
         table.set("get_highest_block_y_at_location", method(table, args -> {
            Location loc = locArg(args, world);
            return (Varargs)(loc != null && loc.getWorld() != null ? LuaValue.valueOf(loc.getWorld().getHighestBlockYAt(loc)) : LuaValue.NIL);
         }));
         table.set("terrain_impact_at_location", method(table, args -> {
            Location location = locArg(args, world);
            return terrainImpact(location, args.arg(2));
         }));
         table.set("terrain_impact", method(table, args -> {
            if (args.narg() < 3) {
               return LuaValue.NIL;
            } else {
               try {
                  Location location = new Location(world, args.checkdouble(1), args.checkdouble(2), args.checkdouble(3));
                  return terrainImpact(location, args.arg(4));
               } catch (RuntimeException var3x) {
                  return LuaValue.NIL;
               }
            }
         }));

         for (BiConsumer<LuaTable, World> enricher : enrichers) {
            try {
               enricher.accept(table, world);
            } catch (Throwable var5) {
            }
         }

         return table;
      }
   }

   private static Location locArg(Varargs args, World world) {
      if (args.narg() >= 1 && args.arg(1).istable()) {
         try {
            return LuaTableSupport.tableToLocation(args.arg(1).checktable(), world);
         } catch (Exception var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static LuaValue terrainImpact(Location location, LuaValue optionsValue) {
      if (location != null && location.getWorld() != null) {
         try {
            TerrainImpactHandle handle;
            if (optionsValue != null && !optionsValue.isnil()) {
               if (!optionsValue.istable()) {
                  return LuaValue.NIL;
               }

               TerrainImpactRequest defaults = new TerrainImpactRequest(location);
               LuaTable options = optionsValue.checktable();
               handle = TerrainImpacts.show(
                  new TerrainImpactRequest(
                     location,
                     options.get("radius").optdouble(defaults.radius()),
                     options.get("intensity").optdouble(defaults.intensity()),
                     options.get("duration_ticks").optint(defaults.durationTicks()),
                     options.get("view_range").optdouble(defaults.viewRange()),
                     options.get("seed").optlong(defaults.seed())
                  )
               );
            } else {
               handle = TerrainImpacts.crack(location);
            }

            return (LuaValue)(active(handle) ? terrainImpactHandle(handle) : LuaValue.NIL);
         } catch (RuntimeException var5) {
            return LuaValue.NIL;
         }
      } else {
         return LuaValue.NIL;
      }
   }

   private static LuaTable terrainImpactHandle(TerrainImpactHandle handle) {
      LuaTable table = new LuaTable();
      table.set("id", handle.id().toString());
      table.set("is_active", method(table, args -> LuaValue.valueOf(active(handle))));
      table.set("remove", method(table, args -> {
         if (!active(handle)) {
            return LuaValue.FALSE;
         } else {
            try {
               handle.close();
               return LuaValue.TRUE;
            } catch (RuntimeException var3) {
               return LuaValue.FALSE;
            }
         }
      }));
      return table;
   }

   private static boolean active(TerrainImpactHandle handle) {
      if (handle == null) {
         return false;
      } else {
         try {
            return handle.active();
         } catch (RuntimeException var2) {
            return false;
         }
      }
   }

   private static VarArgFunction method(final LuaTable owner, final LuaTableSupport.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaTableSupport.stripMethodSelf(args, owner));
         }
      };
   }
}
