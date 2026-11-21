package org.evenmorefish.fish.libs.dimensionfishing.state;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.dimensionfishing.common.TrackedHook;
import org.evenmorefish.fish.libs.dimensionfishing.state.impl.LavaFishingState;
import org.evenmorefish.fish.libs.dimensionfishing.state.impl.NoneFishingState;
import org.evenmorefish.fish.libs.dimensionfishing.state.impl.VoidFishingState;
import org.evenmorefish.fish.libs.dimensionfishing.util.ParticleFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface FishingState {
   NoneFishingState NONE = new NoneFishingState();
   LavaFishingState LAVA = new LavaFishingState();
   VoidFishingState VOID = new VoidFishingState();

   void playBiteSound(@NotNull Player var1);

   void playSwallowSound(@NotNull Player var1);

   @NotNull
   ParticleFactory getLureParticles();

   boolean callEvent(@NotNull TrackedHook var1);

   boolean checkPermission(@NotNull Player var1);

   boolean checkWorld(@NotNull World var1);

   @Nullable
   default World parseWorld(@NotNull String name) {
      if (name.contains(":")) {
         NamespacedKey key = NamespacedKey.fromString(name);
         if (key != null) {
            return Bukkit.getWorld(key);
         }
      }

      return Bukkit.getWorld(name);
   }
}
