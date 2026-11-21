package com.lishid.openinv.internal.paper1_21_10.player;

import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerManager extends com.lishid.openinv.internal.paper26_1.player.PlayerManager {
   public PlayerManager(@NotNull Logger logger) {
      super(logger);
   }

   @Override
   protected void injectPlayer(@NotNull MinecraftServer server, @NotNull ServerPlayer player) throws IllegalAccessException {
      if (this.bukkitEntity != null) {
         this.bukkitEntity.setAccessible(true);
         this.bukkitEntity.set(player, new OpenPlayer(server.server, player, this));
      }
   }
}
