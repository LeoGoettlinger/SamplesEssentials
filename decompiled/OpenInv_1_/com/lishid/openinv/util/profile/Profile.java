package com.lishid.openinv.util.profile;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record Profile(@NotNull String name, @NotNull UUID id) {
   public Profile(@NotNull Player player) {
      this(player.getName(), player.getUniqueId());
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }
}
