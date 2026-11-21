package me.chancesd.pvpmanager.integration.type;

import me.chancesd.pvpmanager.player.ProtectionType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ForceToggleDependency extends Dependency {
   boolean shouldDisable(@NotNull Player var1);

   boolean shouldDisable(@NotNull Player var1, @NotNull Player var2, ProtectionType var3);

   default boolean shouldDisableProtection() {
      return this.getConfigBoolean("No Protection In War", true);
   }
}
