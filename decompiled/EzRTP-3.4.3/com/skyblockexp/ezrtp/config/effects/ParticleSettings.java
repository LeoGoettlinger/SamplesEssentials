package com.skyblockexp.ezrtp.config.effects;

import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

public final class ParticleSettings {
   private final boolean enabled;
   private final Particle particle;
   private final int count;
   private final double offsetX;
   private final double offsetY;
   private final double offsetZ;
   private final double extra;
   private final boolean force;

   public ParticleSettings(boolean enabled, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double extra, boolean force) {
      this.enabled = enabled;
      this.particle = particle;
      this.count = count;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.extra = extra;
      this.force = force;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public Particle getParticle() {
      return this.particle;
   }

   public int getCount() {
      return this.count;
   }

   public double getOffsetX() {
      return this.offsetX;
   }

   public double getOffsetY() {
      return this.offsetY;
   }

   public double getOffsetZ() {
      return this.offsetZ;
   }

   public double getExtra() {
      return this.extra;
   }

   public boolean isForce() {
      return this.force;
   }

   public static ParticleSettings fromConfiguration(ConfigurationSection section, Logger logger, ParticleSettings fallback) {
      if (section == null) {
         return fallback != null ? fallback : disabled();
      } else {
         boolean enabled = section.getBoolean("enabled", fallback != null && fallback.isEnabled());
         String particleName = section.getString("particle", section.getString("type", fallback != null ? fallback.getParticle().name() : "PORTAL"));
         Particle particle = parseParticle(particleName);
         if (particle == null && logger != null) {
            logger.warning("Invalid particle type: " + particleName);
         }

         int count = section.getInt("count", fallback != null ? fallback.getCount() : 40);
         double offsetX = fallback != null ? fallback.getOffsetX() : 0.5;
         double offsetY = fallback != null ? fallback.getOffsetY() : 1.0;
         double offsetZ = fallback != null ? fallback.getOffsetZ() : 0.5;
         if (section.isConfigurationSection("offset")) {
            ConfigurationSection offset = section.getConfigurationSection("offset");
            offsetX = offset.getDouble("x", offsetX);
            offsetY = offset.getDouble("y", offsetY);
            offsetZ = offset.getDouble("z", offsetZ);
         } else {
            offsetX = section.getDouble("offset-x", offsetX);
            offsetY = section.getDouble("offset-y", offsetY);
            offsetZ = section.getDouble("offset-z", offsetZ);
         }

         double extra = section.getDouble("extra", fallback != null ? fallback.getExtra() : 0.0);
         boolean force = section.getBoolean("force", fallback != null && fallback.isForce());
         return new ParticleSettings(enabled, particle, count, offsetX, offsetY, offsetZ, extra, force);
      }
   }

   private static Particle parseParticle(String key) {
      try {
         return Particle.valueOf(key.trim().toUpperCase(Locale.ROOT));
      } catch (NullPointerException | IllegalArgumentException var2) {
         return null;
      }
   }

   public static ParticleSettings disabled() {
      return new ParticleSettings(false, Particle.PORTAL, 40, 0.5, 1.0, 0.5, 0.0, false);
   }
}
