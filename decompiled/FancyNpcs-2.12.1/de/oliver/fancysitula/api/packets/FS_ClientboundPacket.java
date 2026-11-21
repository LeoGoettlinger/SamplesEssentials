package de.oliver.fancysitula.api.packets;

import de.oliver.fancysitula.api.IFancySitula;
import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import org.jetbrains.annotations.ApiStatus;

public abstract class FS_ClientboundPacket {
   @ApiStatus.Internal
   public abstract Object createPacket();

   @ApiStatus.Internal
   protected abstract void sendPacketTo(FS_RealPlayer var1);

   public final void send(FS_RealPlayer player) {
      IFancySitula.LOGGER.debug("Sending packet '" + this.getClass().getSimpleName() + "' to " + player.getBukkitPlayer().getName());
      this.sendPacketTo(player);
   }
}
