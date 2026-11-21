package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Timespan;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Particle.Spell;
import org.bukkit.Particle.Trail;
import org.bukkit.Vibration.Destination;
import org.bukkit.Vibration.Destination.BlockDestination;
import org.bukkit.Vibration.Destination.EntityDestination;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class DataParticles {
   private static final List<EffectInfo<Particle, ?>> PARTICLE_INFOS = new ArrayList<>();

   private static <F, D> void registerParticle(Particle particle, String pattern, D defaultData, Function<F, D> dataFunction, ToString toStringFunction) {
      registerParticle(particle, pattern, (event, expressions, parseResult) -> {
         if (expressions[0] == null) {
            return defaultData;
         } else {
            D data = dataFunction.apply((F)expressions[0].getSingle(event));
            return data == null ? defaultData : data;
         }
      }, toStringFunction);
   }

   private static <D> void registerParticle(Particle particle, String pattern, DataSupplier<D> dataSupplier, ToString toStringFunction) {
      PARTICLE_INFOS.add(new EffectInfo<>(particle, pattern, dataSupplier, toStringFunction));
   }

   @NotNull
   @Unmodifiable
   public static List<EffectInfo<Particle, ?>> getParticleInfos() {
      if (PARTICLE_INFOS.isEmpty()) {
         registerAll();
      }

      return Collections.unmodifiableList(PARTICLE_INFOS);
   }

   private static void registerAll() {
      if (Skript.isRunningMinecraft(1, 21, 9)) {
         DataSupplier<Spell> spellData = (event, expressions, parseResult) -> {
            Color color = (Color)expressions[0].getSingle(event);
            if (color == null) {
               color = ColorRGB.fromBukkitColor(org.bukkit.Color.WHITE);
            }

            Number power = (Number)expressions[1].getSingle(event);
            if (power == null) {
               power = 1.0;
            }

            return new Spell(color.asBukkitColor(), power.floatValue());
         };
         registerParticle(
            Particle.EFFECT,
            "%color% effect particle[s] (of|with) power %number%",
            spellData,
            (exprs, parseResult, builder) -> builder.append(exprs[0], "effect particle of power", exprs[1])
         );
         registerParticle(
            Particle.INSTANT_EFFECT,
            "%color% instant effect particle[s] (of|with) power %number%",
            spellData,
            (exprs, parseResult, builder) -> builder.append(exprs[0], "instant effect particle of power", exprs[1])
         );
         registerParticle(
            Particle.FLASH,
            "%color% flash particle[s]",
            org.bukkit.Color.WHITE,
            color -> ((Color)color).asBukkitColor(),
            (exprs, parseResult, builder) -> builder.append(exprs[0], "flash particle")
         );
      }

      registerParticle(
         Particle.ENTITY_EFFECT,
         "%color% (potion|entity) effect particle[s]",
         org.bukkit.Color.WHITE,
         color -> ((Color)color).asBukkitColor(),
         (exprs, parseResult, builder) -> builder.append(exprs[0], "potion effect particle")
      );
      if (Skript.isRunningMinecraft(1, 21, 5)) {
         registerParticle(
            Particle.TINTED_LEAVES,
            "%color% tinted leaves particle[s]",
            org.bukkit.Color.WHITE,
            color -> ((Color)color).asBukkitColor(),
            (exprs, parseResult, builder) -> builder.append(exprs[0], "tinted leaves particle")
         );
      }

      registerParticle(Particle.DUST, "%color% dust particle[s] [of size %number%]", (event, expressions, parseResult) -> {
         Color color = (Color)expressions[0].getSingle(event);
         org.bukkit.Color bukkitColor;
         if (color == null) {
            bukkitColor = org.bukkit.Color.WHITE;
         } else {
            bukkitColor = color.asBukkitColor();
         }

         Number size = (Number)expressions[1].getSingle(event);
         if (size == null || size.doubleValue() <= 0.0) {
            size = 1.0;
         }

         return new DustOptions(bukkitColor, size.floatValue());
      }, (exprs, parseResult, builder) -> builder.append(exprs[0], "dust particle of size", exprs[1]));
      registerParticle(
         Particle.DUST_COLOR_TRANSITION, "%color% dust particle[s] [of size %number%] that transitions to %color%", (event, expressions, parseResult) -> {
            Color color = (Color)expressions[0].getSingle(event);
            org.bukkit.Color bukkitColor;
            if (color == null) {
               bukkitColor = org.bukkit.Color.WHITE;
            } else {
               bukkitColor = color.asBukkitColor();
            }

            Number size = (Number)expressions[1].getSingle(event);
            if (size == null || size.doubleValue() <= 0.0) {
               size = 1.0;
            }

            Color toColor = (Color)expressions[2].getSingle(event);
            org.bukkit.Color bukkitToColor;
            if (toColor == null) {
               bukkitToColor = org.bukkit.Color.WHITE;
            } else {
               bukkitToColor = toColor.asBukkitColor();
            }

            return new DustTransition(bukkitColor, bukkitToColor, size.floatValue());
         }, (exprs, parseResult, builder) -> builder.append(exprs[0], "dust particle of size", exprs[1], "that transitions to", exprs[2])
      );
      registerParticle(
         Particle.BLOCK,
         "%itemtype/blockdata% block particle[s]",
         DataSupplier::getBlockData,
         (exprs, parseResult, builder) -> builder.append(exprs[0], "block particle")
      );
      if (Skript.isRunningMinecraft(1, 21, 2)) {
         registerParticle(
            Particle.BLOCK_CRUMBLE,
            "%itemtype/blockdata% [block] crumble particle[s]",
            DataSupplier::getBlockData,
            (exprs, parseResult, builder) -> builder.append(exprs[0], "block crumble particle")
         );
      }

      registerParticle(
         Particle.BLOCK_MARKER,
         "%itemtype/blockdata% [block] marker particle[s]",
         DataSupplier::getBlockData,
         (exprs, parseResult, builder) -> builder.append(exprs[0], "block marker particle")
      );
      registerParticle(
         Particle.DUST_PILLAR,
         "%itemtype/blockdata% dust pillar particle[s]",
         DataSupplier::getBlockData,
         (exprs, parseResult, builder) -> builder.append(exprs[0], "dust pillar particle")
      );
      registerParticle(
         Particle.FALLING_DUST,
         "falling %itemtype/blockdata% dust particle[s]",
         DataSupplier::getBlockData,
         (exprs, parseResult, builder) -> builder.append("falling", exprs[0], "dust particle")
      );
      registerParticle(Particle.ITEM, "%itemtype% item particle[s]", (event, expressions, parseResult) -> {
         ItemType itemType = (ItemType)expressions[0].getSingle(event);
         return itemType == null ? new ItemStack(Material.AIR) : itemType.getRandom();
      }, (exprs, parseResult, builder) -> builder.append(exprs[0], "item particle"));
      registerParticle(Particle.SCULK_CHARGE, "sculk charge particle[s] [with [a] roll angle [of] %-number%]", (event, expressions, parseResult) -> {
         if (expressions[0] == null) {
            return 0.0F;
         } else {
            Number angle = (Number)expressions[0].getSingle(event);
            return angle == null ? 0.0F : (float)Math.toRadians(angle.floatValue());
         }
      }, (exprs, parseResult, builder) -> builder.append("sculk charge particle)").appendIf(exprs[0] != null, "with a roll angle of", exprs[0]));
      registerParticle(
         Particle.SHRIEK,
         "shriek particle[s] [delayed by %-timespan%]",
         0,
         timespan -> ((Timespan)timespan).getAs(Timespan.TimePeriod.TICK),
         (exprs, parseResult, builder) -> builder.append("shriek particle").appendIf(exprs[0] != null, "delayed by", exprs[0])
      );
      registerParticle(
         Particle.VIBRATION,
         "vibration particle[s] moving to[wards] %entity/location% [over [a duration of] %-timespan%]",
         (event, expressions, parseResult) -> {
            Object target = expressions[0].getSingle(event);
            Destination destination;
            if (target instanceof Location location) {
               destination = new BlockDestination(location);
            } else {
               if (!(target instanceof Entity entity)) {
                  return null;
               }

               destination = new EntityDestination(entity);
            }

            Timespan timespan = (Timespan)expressions[1].getSingle(event);
            int duration;
            if (timespan == null) {
               duration = 20;
            } else {
               duration = (int)timespan.getAs(Timespan.TimePeriod.TICK);
            }

            return new Vibration(destination, duration);
         },
         (exprs, parseResult, builder) -> builder.append("vibration particle moving towards", exprs[0]).appendIf(exprs[1] != null, "over", exprs[1])
      );
      if (Skript.isRunningMinecraft(1, 21, 4)) {
         registerParticle(
            Particle.TRAIL, "%color% trail particle[s] moving to[wards] %location% [over [a duration of] %-timespan%]", (event, expressions, parseResult) -> {
               Color color = (Color)expressions[0].getSingle(event);
               org.bukkit.Color bukkitColor;
               if (color == null) {
                  bukkitColor = org.bukkit.Color.WHITE;
               } else {
                  bukkitColor = color.asBukkitColor();
               }

               Location targetLocation = (Location)expressions[1].getSingle(event);
               if (targetLocation == null) {
                  return null;
               } else {
                  Number durationTicks = 20;
                  if (expressions[2] != null) {
                     Timespan duration = (Timespan)expressions[2].getSingle(event);
                     if (duration != null) {
                        durationTicks = duration.getAs(Timespan.TimePeriod.TICK);
                     }
                  }

                  return new Trail(targetLocation, bukkitColor, durationTicks.intValue());
               }
            }, (exprs, parseResult, builder) -> builder.append(exprs[0], "trail particle leading to", exprs[1]).appendIf(exprs[2] != null, "over", exprs[2])
         );
      } else if (Skript.isRunningMinecraft(1, 21, 2)) {
         Class<?>[] classes = Particle.class.getClasses();
         Class<?> targetColorClass = null;

         for (Class<?> cls : classes) {
            if (cls.getSimpleName().equals("TargetColor")) {
               targetColorClass = cls;
               break;
            }
         }

         if (targetColorClass != null) {
            try {
               Constructor<?> constructor = targetColorClass.getDeclaredConstructor(Location.class, org.bukkit.Color.class);
               registerParticle(Particle.TRAIL, "[a[n]] %color% trail particle moving to[wards] %location%", (event, expressions, parseResult) -> {
                  Color color = (Color)expressions[0].getSingle(event);
                  org.bukkit.Color bukkitColor;
                  if (color == null) {
                     bukkitColor = org.bukkit.Color.WHITE;
                  } else {
                     bukkitColor = color.asBukkitColor();
                  }

                  Location targetLocation = (Location)expressions[1].getSingle(event);
                  if (targetLocation == null) {
                     return null;
                  } else {
                     try {
                        return constructor.newInstance(targetLocation, bukkitColor);
                     } catch (IllegalAccessException | InvocationTargetException | InstantiationException var8x) {
                        throw new RuntimeException(var8x);
                     }
                  }
               }, (exprs, parseResult, builder) -> builder.append(exprs[0], "trail particle moving to", exprs[1]));
            } catch (NoSuchMethodException var6) {
               throw new RuntimeException(var6);
            }
         }
      }

      if (Skript.isRunningMinecraft(1, 21, 9)) {
         registerParticle(
            Particle.DRAGON_BREATH,
            "dragon breath particle[s] [of power %-number%]",
            0.5F,
            input -> (Float)input,
            (exprs, parseResult, builder) -> builder.append("dragon breath particle").appendIf(exprs[0] != null, "of power", exprs[0])
         );
      }
   }
}
