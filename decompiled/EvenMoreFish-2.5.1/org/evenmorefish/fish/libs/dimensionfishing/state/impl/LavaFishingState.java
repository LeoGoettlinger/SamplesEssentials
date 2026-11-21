package org.evenmorefish.fish.libs.dimensionfishing.state.impl;

import java.util.List;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.dimensionfishing.DimensionFishing;
import org.evenmorefish.fish.libs.dimensionfishing.common.TrackedHook;
import org.evenmorefish.fish.libs.dimensionfishing.events.LavaFishCaughtEvent;
import org.evenmorefish.fish.libs.dimensionfishing.state.FishingState;
import org.evenmorefish.fish.libs.dimensionfishing.util.ParticleFactory;
import org.jspecify.annotations.NonNull;

public class LavaFishingState implements FishingState {
   @Override
   public void playBiteSound(@NonNull Player player) {
      player.playSound(DimensionFishing.getInstance().getConfigProvider().getLavaFishingBiteSound());
   }

   @Override
   public void playSwallowSound(@NonNull Player player) {
      player.playSound(DimensionFishing.getInstance().getConfigProvider().getLavaFishingSwallowSound());
   }

   @NonNull
   @Override
   public ParticleFactory getLureParticles() {
      return DimensionFishing.getInstance().getConfigProvider().getLavaFishingLureParticles();
   }

   @Override
   public boolean callEvent(@NonNull TrackedHook hook) {
      return new LavaFishCaughtEvent(hook).callEvent();
   }

   @Override
   public boolean checkPermission(@NonNull Player player) {
      String permission = DimensionFishing.getInstance().getConfigProvider().getLavaFishingPermission();
      return permission == null || player.hasPermission(permission);
   }

   @Override
   public boolean checkWorld(@NonNull World hookWorld) {
      List<String> worlds = DimensionFishing.getInstance().getConfigProvider().getLavaAllowedWorlds();
      return worlds.isEmpty() ? hookWorld.getEnvironment() == Environment.NETHER : worlds.stream().map(this::parseWorld).anyMatch(hookWorld::equals);
   }
}
