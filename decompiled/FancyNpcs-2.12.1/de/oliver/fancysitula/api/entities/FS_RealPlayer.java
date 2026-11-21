package de.oliver.fancysitula.api.entities;

import de.oliver.fancysitula.api.packets.FS_ClientboundPacket;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FS_RealPlayer {
   @NotNull
   private final Player bukkitPlayer;

   public FS_RealPlayer(@NotNull Player bukkitPlayer) {
      this.bukkitPlayer = bukkitPlayer;
   }

   public void sendPacket(FS_ClientboundPacket packet) {
      packet.send(this);
   }

   @NotNull
   public Player getBukkitPlayer() {
      return this.bukkitPlayer;
   }
}
