package org.skriptlang.skript.bukkit.particles.particleeffects;

import org.bukkit.Particle;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector3d;

public class DirectionalEffect extends ParticleEffect {
   @Internal
   public DirectionalEffect(Particle particle) {
      super(particle);
   }

   public boolean hasVelocity() {
      return this.count() == 0;
   }

   public Vector3d velocity() {
      return this.offset();
   }

   public DirectionalEffect velocity(Vector3d velocity) {
      this.count(0);
      this.offset(velocity);
      return this;
   }

   public DirectionalEffect copy() {
      return (DirectionalEffect)super.copy();
   }
}
