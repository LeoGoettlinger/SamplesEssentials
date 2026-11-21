package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.combatsystem.ScaledCombatRewardResolver;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.instanced.InstanceEffectPolicy;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaWorldTable;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Cone;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Cuboid;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Cylinder;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Dome;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Shape;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Sphere;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.TemporaryBlockManager;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.powers.ProjectileDamage;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import com.magmaguy.elitemobs.utils.shapes.RotatingRay;
import com.magmaguy.elitemobs.utils.shapes.StaticRay;
import com.magmaguy.elitemobs.utils.shapes.TranslatingRay;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

final class LuaPowerSupport {
   private static final double MAX_LOCAL_QUERY_HALF_EXTENT = 128.0;
   private final ScriptDefinition definition;
   private final EliteEntity eliteEntity;

   LuaPowerSupport(ScriptDefinition definition, EliteEntity eliteEntity) {
      this.definition = definition;
      this.eliteEntity = eliteEntity;
   }

   LuaTable toLocationTable(Location location) {
      final LuaTable table = new LuaTable();
      if (location == null) {
         return table;
      } else {
         table.set("x", LuaValue.valueOf(location.getX()));
         table.set("y", LuaValue.valueOf(location.getY()));
         table.set("z", LuaValue.valueOf(location.getZ()));
         table.set("yaw", LuaValue.valueOf((double)location.getYaw()));
         table.set("pitch", LuaValue.valueOf((double)location.getPitch()));
         table.set("direction", this.toVectorTable(location.getDirection()));
         if (location.getWorld() != null) {
            table.set("world", LuaValue.valueOf(location.getWorld().getName()));
         }

         table.set("add", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
               LuaTable target = args.arg1().istable() ? args.arg1().checktable() : table;
               int offsetIndex = args.arg1().raweq(table) ? 2 : 1;
               target.set("x", LuaValue.valueOf(target.get("x").optdouble(0.0) + args.optdouble(offsetIndex, 0.0)));
               target.set("y", LuaValue.valueOf(target.get("y").optdouble(0.0) + args.optdouble(offsetIndex + 1, 0.0)));
               target.set("z", LuaValue.valueOf(target.get("z").optdouble(0.0) + args.optdouble(offsetIndex + 2, 0.0)));
               return target;
            }
         });
         return table;
      }
   }

   Location toLocation(LuaValue value) {
      if (value == null || value.isnil()) {
         return this.eliteEntity.getLocation();
      } else if (!value.istable()) {
         return this.eliteEntity.getLocation();
      } else {
         LuaTable table = value.checktable();
         if (table.get("current_location").istable()) {
            table = table.get("current_location").checktable();
         }

         World world = this.eliteEntity.getLocation() == null ? null : this.eliteEntity.getLocation().getWorld();
         if (table.get("world").isstring()) {
            world = Bukkit.getWorld(table.get("world").tojstring());
         }

         return world == null
            ? null
            : new Location(
               world,
               table.get("x").optdouble(0.0),
               table.get("y").optdouble(0.0),
               table.get("z").optdouble(0.0),
               (float)table.get("yaw").optdouble(0.0),
               (float)table.get("pitch").optdouble(0.0)
            );
      }
   }

   LuaTable toVectorTable(Vector vector) {
      LuaTable table = new LuaTable();
      if (vector == null) {
         return table;
      } else {
         table.set("x", LuaValue.valueOf(vector.getX()));
         table.set("y", LuaValue.valueOf(vector.getY()));
         table.set("z", LuaValue.valueOf(vector.getZ()));
         return table;
      }
   }

   Vector toVector(LuaValue value) {
      if (value != null && !value.isnil() && value.istable()) {
         LuaTable table = value.checktable();
         if (table.get("direction").istable()) {
            table = table.get("direction").checktable();
         }

         return new Vector(
            this.getIndexedOrNamedDouble(table, 1, "x"), this.getIndexedOrNamedDouble(table, 2, "y"), this.getIndexedOrNamedDouble(table, 3, "z")
         );
      } else {
         return null;
      }
   }

   private double getIndexedOrNamedDouble(LuaTable table, int index, String key) {
      LuaValue named = table.get(key);
      if (named.isnumber()) {
         return named.todouble();
      } else {
         LuaValue indexed = table.get(index);
         return indexed.isnumber() ? indexed.todouble() : 0.0;
      }
   }

   float getFloat(Varargs args, int index, float fallback) {
      return args.narg() >= index && args.arg(index).isnumber() ? (float)args.arg(index).todouble() : fallback;
   }

   void playSound(Location location, String soundKey, float volume, float pitch) {
      LuaWorldTable.playSound(location, soundKey, volume, pitch);
   }

   void spawnParticle(Location location, String particleKey, int count) {
      if (location != null && location.getWorld() != null) {
         try {
            Particle particle = Particle.valueOf(particleKey.toUpperCase(Locale.ROOT));
            location.getWorld().spawnParticle(particle, location, count);
         } catch (Exception var5) {
            Logger.warn("Unknown particle " + particleKey + " in Lua power " + this.definition.getFileName() + ".");
         }
      }
   }

   void spawnParticle(Location location, LuaValue particleValue, int fallbackCount) {
      if (particleValue.isstring()) {
         this.spawnParticle(location, particleValue.tojstring(), fallbackCount);
      } else if (location != null && location.getWorld() != null && particleValue.istable()) {
         LuaTable table = particleValue.checktable();
         String particleKey = table.get("particle").optjstring("");
         if (!particleKey.isEmpty()) {
            try {
               Particle particle = Particle.valueOf(particleKey.toUpperCase(Locale.ROOT));
               int amount = table.get("amount").optint(fallbackCount);
               double x = table.get("x").optdouble(0.0);
               double y = table.get("y").optdouble(0.0);
               double z = table.get("z").optdouble(0.0);
               double speed = table.get("speed").optdouble(0.0);
               if (particle.equals(Particle.DUST)) {
                  location.getWorld()
                     .spawnParticle(
                        particle,
                        location,
                        amount,
                        x,
                        y,
                        z,
                        speed,
                        new DustOptions(Color.fromRGB(table.get("red").optint(255), table.get("green").optint(255), table.get("blue").optint(255)), 1.0F)
                     );
               } else if (particle.equals(Particle.DUST_COLOR_TRANSITION)) {
                  location.getWorld()
                     .spawnParticle(
                        particle,
                        location,
                        amount,
                        x,
                        y,
                        z,
                        speed,
                        new DustTransition(
                           Color.fromRGB(table.get("red").optint(255), table.get("green").optint(255), table.get("blue").optint(255)),
                           Color.fromRGB(
                              table.get("toRed").optint(table.get("to_red").optint(255)),
                              table.get("toGreen").optint(table.get("to_green").optint(255)),
                              table.get("toBlue").optint(table.get("to_blue").optint(255))
                           ),
                           1.0F
                        )
                     );
               } else {
                  location.getWorld().spawnParticle(particle, location, amount, x, y, z, speed);
               }
            } catch (Exception var16) {
               Logger.warn("Unknown particle " + particleKey + " in Lua power " + this.definition.getFileName() + ".");
            }
         }
      }
   }

   void applyPotionEffect(LivingEntity livingEntity, String effectKey, int duration, int amplifier) {
      if (InstanceEffectPolicy.canAffect(this.eliteEntity, livingEntity)) {
         PotionEffectType potionEffectType = PotionEffectType.getByName(effectKey.toUpperCase(Locale.ROOT));
         if (potionEffectType == null) {
            Logger.warn("Unknown potion effect " + effectKey + " in Lua power " + this.definition.getFileName() + ".");
         } else {
            livingEntity.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
         }
      }
   }

   void placeTemporaryBlock(Location location, String materialKey, int duration, boolean requireAir) {
      if (location != null && location.getWorld() != null) {
         Material material = this.parseMaterial(materialKey);
         if (material != null) {
            Block block = location.getBlock();
            TemporaryBlockManager.addTemporaryBlock(block, duration, material, requireAir);
         }
      }
   }

   Material parseMaterial(String materialKey) {
      try {
         return Material.valueOf(materialKey.toUpperCase(Locale.ROOT));
      } catch (Exception var3) {
         Logger.warn("Unknown material " + materialKey + " in Lua power " + this.definition.getFileName() + ".");
         return null;
      }
   }

   Collection<LivingEntity> filterEntities(World world, String filter) {
      if (world == null) {
         return Collections.emptyList();
      } else {
         String var3 = filter.toLowerCase(Locale.ROOT);

         return (Collection<LivingEntity>)(switch (var3) {
            case "player", "players" -> new ArrayList(world.getPlayers());
            case "elite", "elites" -> world.getLivingEntities().stream().filter(livingEntity -> EntityTracker.getEliteMobEntity(livingEntity) != null).toList();
            case "mob", "mobs" -> world.getLivingEntities().stream().filter(livingEntity -> !(livingEntity instanceof Player)).toList();
            default -> new ArrayList(world.getLivingEntities());
         });
      }
   }

   Collection<LivingEntity> filterEntities(World world, Shape shape, String filter) {
      return this.filterEntities(world, shape.getEntityQueryBounds(), filter);
   }

   Collection<LivingEntity> filterEntities(Location center, double halfX, double halfY, double halfZ, String filter) {
      World world = center.getWorld();
      if (world == null) {
         return Collections.emptyList();
      } else if (Double.isFinite(halfX)
         && Double.isFinite(halfY)
         && Double.isFinite(halfZ)
         && !(halfX < 0.0)
         && !(halfY < 0.0)
         && !(halfZ < 0.0)
         && !(halfX > 128.0)
         && !(halfY > 128.0)
         && !(halfZ > 128.0)) {
         double minX = center.getX() - halfX;
         double minY = center.getY() - halfY;
         double minZ = center.getZ() - halfZ;
         double maxX = center.getX() + halfX;
         double maxY = center.getY() + halfY;
         double maxZ = center.getZ() + halfZ;
         return Double.isFinite(minX)
               && Double.isFinite(minY)
               && Double.isFinite(minZ)
               && Double.isFinite(maxX)
               && Double.isFinite(maxY)
               && Double.isFinite(maxZ)
            ? this.filterEntities(world, new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ), filter)
            : this.filterEntities(world, filter);
      } else {
         return this.filterEntities(world, filter);
      }
   }

   private Collection<LivingEntity> filterEntities(World world, BoundingBox bounds, String filter) {
      if (world == null) {
         return Collections.emptyList();
      } else if (bounds != null && !(bounds.getWidthX() > 256.0) && !(bounds.getHeight() > 256.0) && !(bounds.getWidthZ() > 256.0)) {
         double minX = Math.nextDown(bounds.getMinX());
         double minY = Math.nextDown(bounds.getMinY());
         double minZ = Math.nextDown(bounds.getMinZ());
         double maxX = Math.nextUp(bounds.getMaxX());
         double maxY = Math.nextUp(bounds.getMaxY());
         double maxZ = Math.nextUp(bounds.getMaxZ());
         if (Double.isFinite(minX) && Double.isFinite(minY) && Double.isFinite(minZ) && Double.isFinite(maxX) && Double.isFinite(maxY) && Double.isFinite(maxZ)
            )
          {
            BoundingBox queryBounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
            String normalizedFilter = filter.toLowerCase(Locale.ROOT);
            return world.getNearbyEntities(queryBounds, entity -> entity instanceof LivingEntity)
               .stream()
               .map(entity -> (LivingEntity)entity)
               .filter(entity -> {
                  return switch (normalizedFilter) {
                     case "player", "players" -> entity instanceof Player;
                     case "elite", "elites" -> EntityTracker.getEliteMobEntity(entity) != null;
                     case "mob", "mobs" -> !(entity instanceof Player);
                     default -> true;
                  };
               })
               .toList();
         } else {
            return this.filterEntities(world, filter);
         }
      } else {
         return this.filterEntities(world, filter);
      }
   }

   void applyVectorOptions(Vector vector, LuaTable options) {
      if (options.get("normalize").optboolean(false) && vector.lengthSquared() > 0.0) {
         vector.normalize();
      }

      vector.multiply(options.get("multiplier").optdouble(1.0));
      Vector offset = this.toVector(options.get("offset"));
      if (offset != null) {
         vector.add(offset);
      }
   }

   Vector rotateVector(Vector original, double pitchDegrees, double yawDegrees) {
      Vector rotated = original.clone();
      rotated.rotateAroundX(Math.toRadians(pitchDegrees));
      rotated.rotateAroundY(Math.toRadians(yawDegrees));
      return rotated;
   }

   <T extends Enum<T>> T parseEnum(String rawValue, Class<T> enumClass, T fallback) {
      try {
         return Enum.valueOf(enumClass, rawValue.toUpperCase(Locale.ROOT));
      } catch (Exception var5) {
         return fallback;
      }
   }

   void applyTag(LivingEntity livingEntity, String tag, boolean add) {
      EliteEntity targetElite = EntityTracker.getEliteMobEntity(livingEntity);
      if (targetElite != null) {
         if (add) {
            targetElite.addTag(tag);
         } else {
            targetElite.removeTag(tag);
         }
      }

      if (livingEntity instanceof Player player) {
         ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
         if (inventory != null) {
            if (add) {
               inventory.addTags(List.of(tag));
            } else {
               inventory.removeTags(List.of(tag));
            }
         }
      }
   }

   boolean hasTag(LivingEntity livingEntity, String tag) {
      EliteEntity targetElite = EntityTracker.getEliteMobEntity(livingEntity);
      if (targetElite != null && targetElite.hasTag(tag)) {
         return true;
      } else if (!(livingEntity instanceof Player player)) {
         return false;
      } else {
         ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
         return inventory != null && inventory.hasTag(tag);
      }
   }

   Firework spawnFirework(Location location, LuaTable spec) {
      if (location != null && location.getWorld() != null) {
         Firework firework = (Firework)location.getWorld().spawn(location, Firework.class);
         firework.setPersistent(false);
         FireworkMeta meta = firework.getFireworkMeta();
         if (spec.get("effects").istable()) {
            LuaTable effects = spec.get("effects").checktable();
            LuaValue key = LuaValue.NIL;

            while (true) {
               Varargs next = effects.next(key);
               key = next.arg1();
               if (key.isnil()) {
                  break;
               }

               if (next.arg(2).istable()) {
                  meta.addEffect(this.buildFireworkEffect(next.arg(2).checktable()));
               }
            }
         } else {
            meta.addEffect(this.buildFireworkEffect(spec));
         }

         meta.setPower(spec.get("power").optint(1));
         firework.setFireworkMeta(meta);
         Vector velocity = this.toVector(spec.get("velocity"));
         if (velocity != null) {
            firework.setVelocity(velocity);
            firework.setShotAtAngle(spec.get("shot_at_angle").optboolean(true));
         }

         return firework;
      } else {
         return null;
      }
   }

   ThrownPotion spawnSplashPotion(Location location, LuaTable spec) {
      if (location != null && location.getWorld() != null) {
         ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
         PotionMeta potionMeta = (PotionMeta)itemStack.getItemMeta();
         if (potionMeta == null) {
            return null;
         } else {
            if (spec.get("effects").istable()) {
               LuaTable effects = spec.get("effects").checktable();
               LuaValue key = LuaValue.NIL;

               while (true) {
                  Varargs next = effects.next(key);
                  key = next.arg1();
                  if (key.isnil()) {
                     break;
                  }

                  if (next.arg(2).istable()) {
                     LuaTable effectSpec = next.arg(2).checktable();
                     String effectKey = effectSpec.get("type").optjstring("").toUpperCase(Locale.ROOT);
                     PotionEffectType effectType = PotionEffectType.getByName(effectKey);
                     if (effectType == null) {
                        Logger.warn("Unknown potion effect " + effectKey + " in Lua power " + this.definition.getFileName() + ".");
                     } else {
                        potionMeta.addCustomEffect(
                           new PotionEffect(effectType, effectSpec.get("duration").optint(0), effectSpec.get("amplifier").optint(0)),
                           effectSpec.get("overwrite").optboolean(true)
                        );
                     }
                  }
               }
            }

            itemStack.setItemMeta(potionMeta);
            ThrownPotion thrownPotion = (ThrownPotion)location.getWorld().spawn(location, ThrownPotion.class);
            thrownPotion.setItem(itemStack);
            Vector velocity = this.toVector(spec.get("velocity"));
            if (velocity != null) {
               thrownPotion.setVelocity(velocity);
            }

            return thrownPotion;
         }
      } else {
         return null;
      }
   }

   float getBlastResistance(Location location) {
      return location != null && location.getWorld() != null ? location.getBlock().getType().getBlastResistance() : 0.0F;
   }

   void generateFakeExplosion(List<Location> blockLocations, Location explosionSourceLocation) {
      if (this.eliteEntity.getLivingEntity() != null && blockLocations != null && !blockLocations.isEmpty()) {
         List<Block> blocks = new ArrayList<>();

         for (Location blockLocation : blockLocations) {
            if (blockLocation != null && blockLocation.getWorld() != null) {
               blocks.add(blockLocation.getBlock());
            }
         }

         PowersConfigFields powersConfigFields = PowersConfig.getPower(this.definition.getFileName());
         Explosion.generateFakeExplosion(blocks, this.eliteEntity.getLivingEntity(), powersConfigFields, explosionSourceLocation);
      }
   }

   void runGoldNuggetDamage(List<ProjectileDamage.FakeProjectile> projectiles) {
      if (projectiles != null && !projectiles.isEmpty()) {
         ProjectileDamage.doGoldNuggetDamage(projectiles, this.eliteEntity);
      }
   }

   void generatePlayerLoot(int times) {
      for (int i = 0; i < times; i++) {
         LootTables.generatePlayerLoot(this.eliteEntity);
      }
   }

   void dropBonusCoins(double coinMultiplier) {
      if (this.eliteEntity.getUnsyncedLivingEntity() != null) {
         for (Player player : this.eliteEntity.getDamagers().keySet()) {
            if (!player.hasMetadata("NPC")
               && PlayerData.isInMemory(player.getUniqueId())
               && !(this.eliteEntity.getDamagers().get(player) / this.eliteEntity.getMaxHealth() <= 0.1)) {
               int rewardLevel = ScaledCombatRewardResolver.getRewardLevel(this.eliteEntity, player);
               new ItemLootShower(rewardLevel * coinMultiplier, rewardLevel, this.eliteEntity.getUnsyncedLivingEntity().getLocation(), player);
            }
         }
      }
   }

   private FireworkEffect buildFireworkEffect(LuaTable spec) {
      String typeName = spec.get("type").optjstring("BALL_LARGE");
      Type type = this.parseEnum(typeName, Type.class, Type.BALL_LARGE);
      Builder builder = FireworkEffect.builder().with(type).flicker(spec.get("flicker").optboolean(true)).trail(spec.get("trail").optboolean(true));
      List<Color> colors = this.parseColors(spec.get("colors"));
      if (!colors.isEmpty()) {
         builder.withColor(colors);
      }

      List<Color> fadeColors = this.parseColors(spec.get("fade_colors"));
      if (!fadeColors.isEmpty()) {
         builder.withFade(fadeColors);
      }

      return builder.build();
   }

   private List<Color> parseColors(LuaValue colorsValue) {
      List<Color> parsedColors = new ArrayList<>();
      if (!colorsValue.istable()) {
         return parsedColors;
      } else {
         LuaTable colors = colorsValue.checktable();
         LuaValue key = LuaValue.NIL;

         while (true) {
            Varargs next = colors.next(key);
            key = next.arg1();
            if (key.isnil()) {
               return parsedColors;
            }

            parsedColors.add(this.parseColor(next.arg(2)));
         }
      }
   }

   Color parseColor(LuaValue value) {
      if (value.isstring()) {
         String var4 = value.tojstring().toUpperCase(Locale.ROOT);

         return switch (var4) {
            case "WHITE" -> Color.WHITE;
            case "SILVER" -> Color.SILVER;
            case "GRAY" -> Color.GRAY;
            case "BLACK" -> Color.BLACK;
            case "RED" -> Color.RED;
            case "MAROON" -> Color.MAROON;
            case "YELLOW" -> Color.YELLOW;
            case "OLIVE" -> Color.OLIVE;
            case "LIME" -> Color.LIME;
            case "GREEN" -> Color.GREEN;
            case "AQUA" -> Color.AQUA;
            case "TEAL" -> Color.TEAL;
            case "BLUE" -> Color.BLUE;
            case "NAVY" -> Color.NAVY;
            case "FUCHSIA" -> Color.FUCHSIA;
            case "PURPLE" -> Color.PURPLE;
            case "ORANGE" -> Color.ORANGE;
            default -> Color.WHITE;
         };
      } else if (value.istable()) {
         LuaTable table = value.checktable();
         return Color.fromRGB(table.get("red").optint(255), table.get("green").optint(255), table.get("blue").optint(255));
      } else {
         return Color.WHITE;
      }
   }

   Entity resolveEntityReference(LuaValue value) {
      if (value != null && !value.isnil() && value.istable()) {
         LuaTable table = value.checktable();
         LuaValue uuidValue = table.rawget("__bukkit_uuid");
         if (!uuidValue.isstring()) {
            uuidValue = table.get("uuid");
         }

         if (!uuidValue.isstring()) {
            return null;
         } else {
            try {
               return Bukkit.getEntity(UUID.fromString(uuidValue.tojstring()));
            } catch (Exception var5) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   LivingEntity resolveLivingEntityReference(LuaValue value) {
      return this.resolveEntityReference(value) instanceof LivingEntity livingEntity ? livingEntity : null;
   }

   Player resolvePlayerReference(LuaValue value) {
      if (this.resolveEntityReference(value) instanceof Player player) {
         return player;
      } else {
         if (value != null && value.istable()) {
            LuaValue uuidValue = value.checktable().get("uuid");
            if (uuidValue.isstring()) {
               try {
                  return Bukkit.getPlayer(UUID.fromString(uuidValue.tojstring()));
               } catch (Exception var5) {
               }
            }
         }

         return null;
      }
   }

   String resolveZoneFilter(LuaValue optionsValue) {
      return optionsValue != null && optionsValue.istable() ? optionsValue.checktable().get("filter").optjstring("living") : "living";
   }

   boolean resolveZoneMode(LuaValue optionsValue) {
      return optionsValue != null && optionsValue.istable() ? "border".equalsIgnoreCase(optionsValue.checktable().get("mode").optjstring("full")) : false;
   }

   String getString(LuaTable table, String... keys) {
      for (String key : keys) {
         LuaValue value = table.get(key);
         if (value.isstring()) {
            return value.tojstring();
         }
      }

      return "";
   }

   double getDouble(LuaTable table, double fallback, String... keys) {
      for (String key : keys) {
         LuaValue value = table.get(key);
         if (value.isnumber()) {
            return value.todouble();
         }
      }

      return fallback;
   }

   int getInt(LuaTable table, int fallback, String... keys) {
      for (String key : keys) {
         LuaValue value = table.get(key);
         if (value.isnumber()) {
            return value.toint();
         }
      }

      return fallback;
   }

   boolean getBoolean(LuaTable table, boolean fallback, String... keys) {
      for (String key : keys) {
         LuaValue value = table.get(key);
         if (value.isboolean()) {
            return value.toboolean();
         }
      }

      return fallback;
   }

   Shape createShape(LuaValue zoneDefinition) {
      if (zoneDefinition.istable() && this.eliteEntity.getLocation() != null) {
         LuaTable table = zoneDefinition.checktable();
         String kind = this.getString(table, "kind", "type");
         Location center = this.resolveLocation(table.get("origin"));
         if (center == null) {
            center = this.eliteEntity.getLocation();
         }

         Location destination = this.resolveLocation(table.get("destination"));
         if (destination == null && center != null) {
            destination = center.clone().add(center.getDirection().normalize().multiply(this.getDouble(table, 0.0, "length")));
         }

         Location originEnd = this.resolveLocation(table.get("origin_end"));
         Location destinationEnd = this.resolveLocation(table.get("destination_end"));
         double radius = this.getDouble(table, 0.0, "radius");
         double borderRadius = this.getDouble(table, 1.0, "border_radius", "borderRadius");
         double height = this.getDouble(table, 0.0, "height");
         double pointRadius = this.getDouble(table, 0.5, "point_radius", "pointRadius", "thickness");
         double x = this.getDouble(table, 0.0, "x");
         double y = this.getDouble(table, 0.0, "y");
         double z = this.getDouble(table, 0.0, "z");
         double xBorder = this.getDouble(table, 1.0, "x_border", "xBorder");
         double yBorder = this.getDouble(table, 1.0, "y_border", "yBorder");
         double zBorder = this.getDouble(table, 1.0, "z_border", "zBorder");
         double pitchPreRotation = this.getDouble(table, 0.0, "pitch_pre_rotation", "pitchPreRotation");
         double yawPreRotation = this.getDouble(table, 0.0, "yaw_pre_rotation", "yawPreRotation");
         double pitchRotation = this.getDouble(table, 0.0, "pitch_rotation", "pitchRotation");
         double yawRotation = this.getDouble(table, 0.0, "yaw_rotation", "yawRotation");
         int animationDuration = this.getInt(table, 0, "animation_duration", "animationDuration");
         boolean ignoresSolidBlocks = this.getBoolean(table, true, "ignores_solid_blocks", "ignoresSolidBlocks");

         return (Shape)(switch (kind) {
            case "sphere" -> new Sphere(radius, center, borderRadius);
            case "dome" -> new Dome(radius, center, borderRadius);
            case "cylinder" -> new Cylinder(center, radius, height, borderRadius);
            case "cuboid" -> new Cuboid((float)x, (float)y, (float)z, (float)xBorder, (float)yBorder, (float)zBorder, center);
            case "cone" -> destination == null ? null : new Cone(center.clone(), destination, radius, borderRadius);
            case "static_ray" -> destination == null ? null : new StaticRay(ignoresSolidBlocks, pointRadius, center.clone(), destination);
            case "rotating_ray" -> destination == null
               ? null
               : new RotatingRay(
                  ignoresSolidBlocks, pointRadius, center.clone(), destination, pitchPreRotation, yawPreRotation, pitchRotation, yawRotation, animationDuration
               );
            case "translating_ray" -> destination == null
               ? null
               : new TranslatingRay(ignoresSolidBlocks, pointRadius, center.clone(), originEnd, destination, destinationEnd, animationDuration);
            default -> null;
         });
      } else {
         return null;
      }
   }

   LuaTable toParticleSpec(Varargs args) {
      LuaTable particle = new LuaTable();
      particle.set("particle", args.arg(2).isstring() ? args.arg(2) : args.arg(1));
      particle.set("amount", LuaValue.valueOf(args.narg() >= 3 ? args.arg(3).optint(1) : 1));
      particle.set("x", LuaValue.valueOf(args.narg() >= 4 ? args.arg(4).optdouble(0.0) : 0.0));
      particle.set("y", LuaValue.valueOf(args.narg() >= 5 ? args.arg(5).optdouble(0.0) : 0.0));
      particle.set("z", LuaValue.valueOf(args.narg() >= 6 ? args.arg(6).optdouble(0.0) : 0.0));
      particle.set("speed", LuaValue.valueOf(args.narg() >= 7 ? args.arg(7).optdouble(0.0) : 0.0));
      return particle;
   }

   private Location resolveLocation(LuaValue value) {
      return this.toLocation(value);
   }
}
