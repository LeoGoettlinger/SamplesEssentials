package io.github.retrooper.packetevents.impl.netty.manager.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerManagerAbstract implements PlayerManager {
   @Override
   public abstract int getPing(@NotNull Object player);

   @Override
   public abstract Object getChannel(@NotNull Object player);

   @NotNull
   @Override
   public ClientVersion getClientVersion(@NotNull Object player) {
      return this.getUser(player).getClientVersion();
   }

   @Override
   public User getUser(@NotNull Object player) {
      Object channel = this.getChannel(player);
      return PacketEvents.getAPI().getProtocolManager().getUser(channel);
   }
}
