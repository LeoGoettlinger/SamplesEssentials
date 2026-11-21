package com.magmaguy.elitemobs.magmacore.util;

import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

public final class OwnedEntityState implements AutoCloseable {
   private final Entity entity;
   private final Plugin plugin;
   private final String key;
   private final String token = UUID.randomUUID().toString();
   private final BooleanSupplier unchanged;
   private final Runnable restore;
   private boolean closed;

   private OwnedEntityState(Entity entity, Plugin plugin, String property, BooleanSupplier unchanged, Runnable restore) {
      this.entity = entity;
      this.plugin = plugin;
      this.key = "nightbreak_owned_" + property;
      this.unchanged = unchanged;
      this.restore = restore;
      entity.setMetadata(this.key, new FixedMetadataValue(plugin, this.token));
   }

   public static OwnedEntityState gravity(Entity entity, boolean replacement, Plugin plugin) {
      if (!available(entity, plugin, "gravity")) {
         return null;
      } else {
         boolean original = entity.hasGravity();
         OwnedEntityState lease = new OwnedEntityState(entity, plugin, "gravity", () -> entity.hasGravity() == replacement, () -> entity.setGravity(original));

         try {
            entity.setGravity(replacement);
            return lease;
         } catch (RuntimeException var6) {
            lease.close();
            throw var6;
         }
      }
   }

   public static OwnedEntityState scale(LivingEntity entity, double replacement, Plugin plugin) {
      if (!Double.isFinite(replacement) || replacement <= 0.0) {
         throw new IllegalArgumentException("Invalid scale");
      } else if (!available(entity, plugin, "scale")) {
         return null;
      } else {
         Attribute attribute = (Attribute)Registry.ATTRIBUTE.get(NamespacedKey.minecraft("scale"));
         if (attribute == null) {
            attribute = (Attribute)Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.scale"));
         }

         AttributeInstance instance = attribute == null ? null : entity.getAttribute(attribute);
         if (instance == null) {
            return null;
         } else {
            double original = instance.getBaseValue();
            OwnedEntityState lease = new OwnedEntityState(
               entity, plugin, "scale", () -> Double.compare(instance.getBaseValue(), replacement) == 0, () -> instance.setBaseValue(original)
            );

            try {
               instance.setBaseValue(replacement);
               return lease;
            } catch (RuntimeException var10) {
               lease.close();
               throw var10;
            }
         }
      }
   }

   public static OwnedEntityState potion(LivingEntity entity, PotionEffect replacement, Plugin plugin) {
      if (replacement.getDuration() < 1) {
         throw new IllegalArgumentException("Owned potion duration must be positive");
      } else {
         String property = "potion_" + replacement.getType().getKey().getKey();
         if (!available(entity, plugin, property)) {
            return null;
         } else {
            PotionEffect original = entity.getPotionEffect(replacement.getType());
            int started = entity.getTicksLived();
            OwnedEntityState lease = new OwnedEntityState(
               entity,
               plugin,
               property,
               () -> samePotion(entity.getPotionEffect(replacement.getType()), replacement, Math.max(0, entity.getTicksLived() - started)),
               () -> {
                  entity.removePotionEffect(replacement.getType());
                  if (original != null) {
                     int elapsed = Math.max(0, entity.getTicksLived() - started);
                     int remaining = original.getDuration() < 0 ? original.getDuration() : original.getDuration() - elapsed;
                     if (remaining > 0 || original.getDuration() < 0) {
                        entity.addPotionEffect(
                           new PotionEffect(
                              original.getType(), remaining, original.getAmplifier(), original.isAmbient(), original.hasParticles(), original.hasIcon()
                           )
                        );
                     }
                  }
               }
            );

            try {
               if (entity.addPotionEffect(replacement) && samePotion(entity.getPotionEffect(replacement.getType()), replacement, 0)) {
                  return lease;
               } else {
                  lease.close(false);
                  return null;
               }
            } catch (RuntimeException var8) {
               lease.close();
               throw var8;
            }
         }
      }
   }

   private static boolean samePotion(PotionEffect current, PotionEffect applied, int elapsed) {
      return current != null
         && current.getType().equals(applied.getType())
         && current.getAmplifier() == applied.getAmplifier()
         && current.isAmbient() == applied.isAmbient()
         && current.hasParticles() == applied.hasParticles()
         && current.hasIcon() == applied.hasIcon()
         && Math.abs(current.getDuration() - ((long)applied.getDuration() - elapsed)) <= 1L;
   }

   private static boolean available(Entity entity, Plugin plugin, String property) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Entity ownership requires the server thread");
      } else {
         return plugin.isEnabled() && entity != null && entity.isValid() && !entity.hasMetadata("nightbreak_owned_" + property);
      }
   }

   @Override
   public void close() {
      this.close(true);
   }

   private void close(boolean restoreState) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Entity restoration requires the server thread");
      } else if (!this.closed) {
         this.closed = true;
         boolean owned = this.entity
            .getMetadata(this.key)
            .stream()
            .anyMatch(value -> value.getOwningPlugin() == this.plugin && this.token.equals(value.asString()));
         if (owned) {
            this.entity.removeMetadata(this.key, this.plugin);
            if (restoreState && this.entity.isValid() && this.unchanged.getAsBoolean()) {
               this.restore.run();
            }
         }
      }
   }
}
