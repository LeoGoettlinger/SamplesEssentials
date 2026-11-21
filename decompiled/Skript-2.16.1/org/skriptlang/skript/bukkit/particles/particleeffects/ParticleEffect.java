package org.skriptlang.skript.bukkit.particles.particleeffects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumParser;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.destroystokyo.paper.ParticleBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.skriptlang.skript.bukkit.particles.ParticleUtils;

public class ParticleEffect extends ParticleBuilder implements Debuggable {
   private static final ParticleEffect.ParticleParser ENUM_PARSER = new ParticleEffect.ParticleParser();
   private static final Pattern LEADING_NUMBER_PATTERN = Pattern.compile("(\\d+) (.+)");

   @Contract("_ -> new")
   @NotNull
   public static ParticleEffect of(Particle particle) {
      if (ParticleUtils.isConverging(particle)) {
         return new ConvergingEffect(particle);
      } else if (ParticleUtils.usesVelocity(particle)) {
         return new DirectionalEffect(particle);
      } else {
         return (ParticleEffect)(ParticleUtils.isScalable(particle) ? new ScalableEffect(particle) : new ParticleEffect(particle));
      }
   }

   @Contract("_ -> new")
   @NotNull
   public static ParticleEffect of(@NotNull ParticleBuilder builder) {
      Particle particle = builder.particle();
      ParticleEffect effect = of(particle);
      effect.count(builder.count());
      effect.data(builder.data());
      Location loc;
      if ((loc = builder.location()) != null) {
         effect.location(loc);
      }

      effect.offset(builder.offsetX(), builder.offsetY(), builder.offsetZ());
      effect.extra(builder.extra());
      effect.force(builder.force());
      effect.receivers(builder.receivers());
      effect.source(builder.source());
      return effect;
   }

   @Nullable
   public static ParticleEffect parse(String input, ParseContext context) {
      Matcher matcher = LEADING_NUMBER_PATTERN.matcher(input);
      int count = 1;
      if (matcher.matches()) {
         try {
            count = Math.clamp((long)Integer.parseInt(matcher.group(1)), 0, 16384);
         } catch (NumberFormatException var5) {
            return null;
         }

         input = matcher.group(2);
      }

      Particle particle = ENUM_PARSER.parse(input.toLowerCase(Locale.ENGLISH), context);
      if (particle == null) {
         return null;
      } else if (particle.getDataType() != Void.class) {
         Skript.error("The " + Classes.toString(particle) + " requires data and cannot be parsed directly. Use the Particle With Data expression instead.");
         return null;
      } else {
         return of(particle).count(count);
      }
   }

   public static String toString(Particle particle, int flags) {
      return ENUM_PARSER.toString(particle, flags);
   }

   @NotNull
   public static String[] getAllNamesWithoutData() {
      return ENUM_PARSER.getPatternsWithoutData();
   }

   protected ParticleEffect(Particle particle) {
      super(particle);
   }

   public ParticleEffect spawn() {
      return this.dataType() != Void.class && !this.dataType().isInstance(this.data()) ? this : (ParticleEffect)super.spawn();
   }

   public ParticleEffect spawn(Location location) {
      this.location(location).spawn();
      return this;
   }

   public Vector3d offset() {
      return new Vector3d(this.offsetX(), this.offsetY(), this.offsetZ());
   }

   public ParticleEffect offset(@NotNull Vector3d offset) {
      return (ParticleEffect)super.offset(offset.x(), offset.y(), offset.z());
   }

   public ParticleEffect receivers(@NotNull Vector3i radii) {
      return (ParticleEffect)super.receivers(radii.x(), radii.y(), radii.z());
   }

   public ParticleEffect receivers(@NotNull Vector3d radii) {
      return (ParticleEffect)super.receivers((int)radii.x(), (int)radii.y(), (int)radii.z());
   }

   public boolean isUsingNormalDistribution() {
      return this.count() != 0;
   }

   public Vector3d distribution() {
      return this.offset();
   }

   public ParticleEffect distribution(Vector3d distribution) {
      if (!this.isUsingNormalDistribution()) {
         this.count(1);
      }

      return this.offset(distribution);
   }

   public <T> ParticleEffect data(@Nullable T data) {
      return data != null && !this.dataType().isInstance(data) ? this : (ParticleEffect)super.data(data);
   }

   public boolean acceptsData(@Nullable Object data) {
      return data == null ? true : this.dataType().isInstance(data);
   }

   public Class<?> dataType() {
      return this.particle().getDataType();
   }

   public ParticleEffect copy() {
      return (ParticleEffect)this.clone();
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return ENUM_PARSER.toString(this.particle(), 0);
   }

   public ParticleEffect particle(Particle particle) {
      return (ParticleEffect)super.particle(particle);
   }

   public ParticleEffect allPlayers() {
      return (ParticleEffect)super.allPlayers();
   }

   public ParticleEffect receivers(@Nullable List<Player> receivers) {
      return (ParticleEffect)super.receivers(receivers);
   }

   public ParticleEffect receivers(@Nullable Collection<Player> receivers) {
      return (ParticleEffect)super.receivers(receivers);
   }

   public ParticleEffect receivers(@Nullable Player... receivers) {
      return (ParticleEffect)super.receivers(receivers);
   }

   public ParticleEffect receivers(int radius) {
      return (ParticleEffect)super.receivers(radius);
   }

   public ParticleEffect receivers(int radius, boolean byDistance) {
      return (ParticleEffect)super.receivers(radius, byDistance);
   }

   public ParticleEffect receivers(int xzRadius, int yRadius) {
      return (ParticleEffect)super.receivers(xzRadius, yRadius);
   }

   public ParticleEffect receivers(int xzRadius, int yRadius, boolean byDistance) {
      return (ParticleEffect)super.receivers(xzRadius, yRadius, byDistance);
   }

   public ParticleEffect receivers(int xRadius, int yRadius, int zRadius) {
      return (ParticleEffect)super.receivers(xRadius, yRadius, zRadius);
   }

   public ParticleEffect source(@Nullable Player source) {
      return (ParticleEffect)super.source(source);
   }

   public ParticleEffect location(Location location) {
      return (ParticleEffect)super.location(location);
   }

   public ParticleEffect location(World world, double x, double y, double z) {
      return (ParticleEffect)super.location(world, x, y, z);
   }

   public ParticleEffect count(int count) {
      return (ParticleEffect)super.count(count);
   }

   public ParticleEffect offset(double offsetX, double offsetY, double offsetZ) {
      return (ParticleEffect)super.offset(offsetX, offsetY, offsetZ);
   }

   public ParticleEffect extra(double extra) {
      return (ParticleEffect)super.extra(extra);
   }

   public ParticleEffect force(boolean force) {
      return (ParticleEffect)super.force(force);
   }

   private static class ParticleParser extends EnumParser<Particle> {
      public ParticleParser() {
         super(Particle.class, "particle");
      }

      @NotNull
      public String[] getPatternsWithoutData() {
         return this.parseMap.entrySet().stream().filter(entry -> {
            Particle particle = entry.getValue();
            return particle.getDataType() == Void.class;
         }).map(Entry::getKey).toArray(String[]::new);
      }
   }
}
