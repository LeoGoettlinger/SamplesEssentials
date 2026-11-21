package org.evenmorefish.fish.libs.dimensionfishing.state.impl;

import java.util.List;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.dimensionfishing.DimensionFishing;
import org.evenmorefish.fish.libs.dimensionfishing.common.TrackedHook;
import org.evenmorefish.fish.libs.dimensionfishing.events.VoidFishCaughtEvent;
import org.evenmorefish.fish.libs.dimensionfishing.state.FishingState;
import org.evenmorefish.fish.libs.dimensionfishing.util.ParticleFactory;
import org.jspecify.annotations.NonNull;

public class VoidFishingState implements FishingState {
   @Override
   public void playBiteSound(@NonNull Player player) {
      player.playSound(DimensionFishing.getInstance().getConfigProvider().getVoidFishingBiteSound());
   }

   @Override
   public void playSwallowSound(@NonNull Player player) {
      player.playSound(DimensionFishing.getInstance().getConfigProvider().getVoidFishingSwallowSound());
   }

   @NonNull
   @Override
   public ParticleFactory getLureParticles() {
      return DimensionFishing.getInstance().getConfigProvider().getVoidFishingLureParticles();
   }

   @Override
   public boolean callEvent(@NonNull TrackedHook hook) {
      return new VoidFishCaughtEvent(hook).callEvent();
   }

   @Override
   public boolean checkPermission(@NonNull Player player) {
      String permission = DimensionFishing.getInstance().getConfigProvider().getVoidFishingPermission();
      return permission == null || player.hasPermission(permission);
   }

   @Override
   public boolean checkWorld(@NonNull World hookWorld) {
      List<String> worlds = DimensionFishing.getInstance().getConfigProvider().getVoidAllowedWorlds();
      return worlds.isEmpty() ? hookWorld.getEnvironment() == Environment.THE_END : worlds.stream().map(this::parseWorld).anyMatch(hookWorld::equals);
   }
}
