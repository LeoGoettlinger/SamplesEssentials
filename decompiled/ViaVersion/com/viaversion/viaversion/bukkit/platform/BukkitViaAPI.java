package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;

public class BukkitViaAPI extends ViaAPIBase<Player> {
   public ProtocolVersion getPlayerProtocolVersion(Player player) {
      return this.getPlayerProtocolVersion(player.getUniqueId());
   }

   public void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException {
      this.sendRawPacket(player.getUniqueId(), packet);
   }
}
