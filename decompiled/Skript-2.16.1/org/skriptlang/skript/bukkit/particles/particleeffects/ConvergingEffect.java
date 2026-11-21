package org.skriptlang.skript.bukkit.particles.particleeffects;

import org.bukkit.Particle;
import org.jetbrains.annotations.ApiStatus.Internal;

public class ConvergingEffect extends ParticleEffect {
   @Internal
   public ConvergingEffect(Particle particle) {
      super(particle);
   }

   public ConvergingEffect copy() {
      return (ConvergingEffect)super.copy();
   }
}
