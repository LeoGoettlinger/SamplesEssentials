package com.skyblockexp.ezrtp.config.effects;

import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

public final class CountdownParticleSettings {
   private final boolean enabled;
   private final Particle particle;
   private final int points;
   private final double radius;
   private final double heightOffset;
   private final double extra;
   private final boolean force;
   private final Particle secondaryParticle;
   private final int secondaryCount;
   private final double secondaryOffset;

   public CountdownParticleSettings(
      boolean enabled,
      Particle particle,
      int points,
      double radius,
      double heightOffset,
      double extra,
      boolean force,
      Particle secondaryParticle,
      int secondaryCount,
      double secondaryOffset
   ) {
      this.enabled = enabled;
      this.particle = particle;
      this.points = points;
      this.radius = radius;
      this.heightOffset = heightOffset;
      this.extra = extra;
      this.force = force;
      this.secondaryParticle = secondaryParticle;
      this.secondaryCount = secondaryCount;
      this.secondaryOffset = secondaryOffset;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public Particle getParticle() {
      return this.particle;
   }

   public int getPoints() {
      return this.points;
   }

   public double getRadius() {
      return this.radius;
   }

   public double getHeightOffset() {
      return this.heightOffset;
   }

   public double getExtra() {
      return this.extra;
   }

   public boolean isForce() {
      return this.force;
   }

   public Particle getSecondaryParticle() {
      return this.secondaryParticle;
   }

   public int getSecondaryCount() {
      return this.secondaryCount;
   }

   public double getSecondaryOffset() {
      return this.secondaryOffset;
   }

   public static CountdownParticleSettings fromConfiguration(ConfigurationSection section, Logger logger) {
      if (section == null) {
         return disabled();
      } else {
         boolean enabled = section.getBoolean("enabled", false);
         Particle particle = parseParticle(section.getString("particle", section.getString("type", "ENCHANT")), logger, Particle.ENCHANT);
         int points = Math.max(1, section.getInt("points", 12));
         double radius = section.getDouble("radius", 1.2);
         double heightOffset = section.getDouble("height-offset", 0.8);
         double extra = section.getDouble("extra", 0.0);
         boolean force = section.getBoolean("force", false);
         String secondaryName = section.getString("secondary-particle", "");
         Particle secondaryParticle = secondaryName != null && !secondaryName.isBlank() ? parseParticle(secondaryName, logger, null) : null;
         int secondaryCount = Math.max(0, section.getInt("secondary-count", 6));
         double secondaryOffset = section.getDouble("secondary-offset", 0.35);
         return new CountdownParticleSettings(enabled, particle, points, radius, heightOffset, extra, force, secondaryParticle, secondaryCount, secondaryOffset);
      }
   }

   public static CountdownParticleSettings disabled() {
      return new CountdownParticleSettings(false, Particle.ENCHANT, 12, 1.2, 0.8, 0.0, false, null, 0, 0.35);
   }

   private static Particle parseParticle(String name, Logger logger, Particle fallback) {
      try {
         return Particle.valueOf(name.trim().toUpperCase(Locale.ROOT));
      } catch (NullPointerException | IllegalArgumentException var4) {
         if (logger != null) {
            logger.warning("Invalid particle type: " + name);
         }

         return fallback;
      }
   }
}
