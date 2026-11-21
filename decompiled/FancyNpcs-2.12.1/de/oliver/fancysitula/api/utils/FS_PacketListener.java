package de.oliver.fancysitula.api.utils;

import de.oliver.fancysitula.api.packets.FS_ServerboundPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public abstract class FS_PacketListener {
   protected final FS_ServerboundPacket.Type packet;
   protected final List<Consumer<FS_PacketListener.PacketReceivedEvent>> listeners;

   public FS_PacketListener(FS_ServerboundPacket.Type packet) {
      this.packet = packet;
      this.listeners = new ArrayList<>();
   }

   public abstract void inject(Player var1);

   public void addListener(Consumer<FS_PacketListener.PacketReceivedEvent> listener) {
      this.listeners.add(listener);
   }

   public FS_ServerboundPacket.Type getPacket() {
      return this.packet;
   }

   public record PacketReceivedEvent(FS_ServerboundPacket packet, Player player) {
   }
}
