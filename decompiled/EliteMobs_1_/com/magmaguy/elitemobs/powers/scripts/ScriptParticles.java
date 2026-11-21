package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptParticlesBlueprint;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ScriptParticles {
   private static final Set<String> UNSUPPORTED_PARTICLE_DATA_WARNINGS = ConcurrentHashMap.newKeySet();
   ScriptParticlesBlueprint particlesBlueprint;

   public ScriptParticles(ScriptParticlesBlueprint particlesBlueprint) {
      this.particlesBlueprint = particlesBlueprint;
   }

   public void visualize(ScriptActionData scriptActionData, Location location, ScriptRuntimeOwner runtimeOwner) {
      this.particlesBlueprint
         .getParticleBlueprints()
         .forEach(particleBlueprint -> new ScriptParticles.ScriptParticle(particleBlueprint).visualize(scriptActionData, location, runtimeOwner));
   }

   private class ScriptParticle {
      private final ScriptParticlesBlueprint.ScriptParticleBlueprint particleBlueprint;

      public ScriptParticle(ScriptParticlesBlueprint.ScriptParticleBlueprint scriptParticlesBlueprint) {
         this.particleBlueprint = scriptParticlesBlueprint;
      }

      private void visualize(ScriptActionData scriptActionData, Location location, ScriptRuntimeOwner runtimeOwner) {
         if (location.getWorld() != null) {
            double x = this.particleBlueprint.getX().getValue().floatValue();
            double y = this.particleBlueprint.getY().getValue().floatValue();
            double z = this.particleBlueprint.getZ().getValue().floatValue();
            int amount = this.particleBlueprint.getAmount().getValue();
            if (this.particleBlueprint.getRelativeVectorBlueprint() != null) {
               ScriptRelativeVector scriptRelativeVector = null;
               scriptRelativeVector = new ScriptRelativeVector(this.particleBlueprint.getRelativeVectorBlueprint(), runtimeOwner, location);
               Vector movementVector = scriptRelativeVector.getVector(scriptActionData);
               amount = 0;
               x = movementVector.getX();
               y = movementVector.getY();
               z = movementVector.getZ();
            }

            if (this.particleBlueprint.getParticle().equals(Particle.DUST)) {
               location.getWorld()
                  .spawnParticle(
                     this.particleBlueprint.getParticle(),
                     location,
                     amount,
                     x,
                     y,
                     z,
                     this.particleBlueprint.getSpeed().getValue().floatValue(),
                     new DustOptions(
                        Color.fromRGB(
                           this.particleBlueprint.getRed().getValue(),
                           this.particleBlueprint.getGreen().getValue(),
                           this.particleBlueprint.getBlue().getValue()
                        ),
                        1.0F
                     )
                  );
            } else if (this.particleBlueprint.getParticle().equals(Particle.DUST_COLOR_TRANSITION)) {
               location.getWorld()
                  .spawnParticle(
                     this.particleBlueprint.getParticle(),
                     location,
                     amount,
                     x,
                     y,
                     z,
                     this.particleBlueprint.getSpeed().getValue().floatValue(),
                     new DustTransition(
                        Color.fromRGB(
                           this.particleBlueprint.getRed().getValue(),
                           this.particleBlueprint.getGreen().getValue(),
                           this.particleBlueprint.getBlue().getValue()
                        ),
                        Color.fromRGB(
                           this.particleBlueprint.getToRed().getValue(),
                           this.particleBlueprint.getToGreen().getValue(),
                           this.particleBlueprint.getToBlue().getValue()
                        ),
                        1.0F
                     )
                  );
            } else if (this.particleBlueprint.getDataType() == Color.class) {
               location.getWorld()
                  .spawnParticle(
                     this.particleBlueprint.getParticle(),
                     location,
                     amount,
                     x,
                     y,
                     z,
                     this.particleBlueprint.getSpeed().getValue().floatValue(),
                     Color.fromRGB(
                        this.particleBlueprint.getRed().getValue(), this.particleBlueprint.getGreen().getValue(), this.particleBlueprint.getBlue().getValue()
                     )
                  );
            } else {
               Class<?> dataType = this.particleBlueprint.getDataType();
               if (dataType == Void.class) {
                  location.getWorld()
                     .spawnParticle(this.particleBlueprint.getParticle(), location, amount, x, y, z, this.particleBlueprint.getSpeed().getValue().floatValue());
                  return;
               }

               Object data = this.createParticleData(dataType);
               if (data == null) {
                  this.warnUnsupportedParticleData(dataType);
                  return;
               }

               location.getWorld()
                  .spawnParticle(
                     this.particleBlueprint.getParticle(), location, amount, x, y, z, this.particleBlueprint.getSpeed().getValue().floatValue(), data
                  );
            }
         }
      }

      private Object createParticleData(Class<?> dataType) {
         if (BlockData.class.isAssignableFrom(dataType)) {
            return this.particleBlueprint.getBlockData();
         } else if (ItemStack.class.isAssignableFrom(dataType)) {
            return this.particleBlueprint.getItemStack();
         } else if (dataType == Float.class) {
            return this.particleBlueprint.getSpeed().getValue();
         } else {
            return dataType == Integer.class ? 0 : null;
         }
      }

      private void warnUnsupportedParticleData(Class<?> dataType) {
         String warningKey = this.particleBlueprint.getParticle().name() + ":" + dataType.getName();
         if (ScriptParticles.UNSUPPORTED_PARTICLE_DATA_WARNINGS.add(warningKey)) {
            Logger.warn(
               "Skipping script particle "
                  + this.particleBlueprint.getParticle().name()
                  + " in script "
                  + this.particleBlueprint.getScriptName()
                  + " because Bukkit requires unsupported "
                  + dataType.getSimpleName()
                  + " data."
            );
         }
      }
   }
}
