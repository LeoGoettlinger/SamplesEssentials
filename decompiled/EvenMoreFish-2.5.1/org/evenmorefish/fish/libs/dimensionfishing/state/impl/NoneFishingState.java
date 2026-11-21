package org.evenmorefish.fish.libs.dimensionfishing.state.impl;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.dimensionfishing.common.TrackedHook;
import org.evenmorefish.fish.libs.dimensionfishing.state.FishingState;
import org.evenmorefish.fish.libs.dimensionfishing.util.ParticleFactory;
import org.jspecify.annotations.NonNull;

public class NoneFishingState implements FishingState {
   private final ParticleFactory lureParticles = new ParticleFactory();

   @Override
   public void playBiteSound(@NonNull Player player) {
   }

   @Override
   public void playSwallowSound(@NonNull Player player) {
   }

   @NonNull
   @Override
   public ParticleFactory getLureParticles() {
      return this.lureParticles;
   }

   @Override
   public boolean callEvent(@NonNull TrackedHook hook) {
      return true;
   }

   @Override
   public boolean checkPermission(@NonNull Player player) {
      return true;
   }

   @Override
   public boolean checkWorld(@NonNull World hookWorld) {
      return true;
   }
}
