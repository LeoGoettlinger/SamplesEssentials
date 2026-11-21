package com.magmaguy.elitemobs.easyminecraftgoals.v26.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.CraftBukkitBridge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PacketBundle implements AbstractPacketBundle {
   private final List<PacketBundle.PacketBundleEntry> entries = new ArrayList<>();
   private static final int MAX_PACKETS_PER_BUNDLE = 3000;

   @Override
   public void addPacket(Object packet, List<Player> viewers) {
      this.entries.add(new PacketBundle.PacketBundleEntry((Packet<?>)packet, viewers));
   }

   @Override
   public void send() {
      Map<Player, List<Packet<ClientGamePacketListener>>> playerPackets = new HashMap<>();

      for (int entryIndex = 0; entryIndex < this.entries.size(); entryIndex++) {
         PacketBundle.PacketBundleEntry entry = this.entries.get(entryIndex);
         if (!entry.viewers().isEmpty() && this.isClientGamePacket(entry.packet())) {
            Packet<ClientGamePacketListener> clientPacket = (Packet<ClientGamePacketListener>)entry.packet();
            List<Player> viewers = entry.viewers();
            if (viewers instanceof RandomAccess) {
               for (int viewerIndex = 0; viewerIndex < viewers.size(); viewerIndex++) {
                  playerPackets.computeIfAbsent(viewers.get(viewerIndex), k -> new ArrayList<>()).add(clientPacket);
               }
            } else {
               for (Player viewer : viewers) {
                  playerPackets.computeIfAbsent(viewer, k -> new ArrayList<>()).add(clientPacket);
               }
            }
         }
      }

      playerPackets.forEach((player, packets) -> {
         if (!packets.isEmpty() && player != null && player.isOnline()) {
            for (int i = 0; i < packets.size(); i += 3000) {
               int end = Math.min(i + 3000, packets.size());
               List<Packet<ClientGamePacketListener>> chunk = packets.subList(i, end);
               ClientboundBundlePacket bundle = new ClientboundBundlePacket(new ArrayList<>(chunk));
               this.sendPacketBundle(player, bundle);
            }
         }
      });
   }

   private boolean isClientGamePacket(Packet<?> packet) {
      return packet != null;
   }

   private void sendPacketBundle(Player player, Packet<?> nmsPacket) {
      if (nmsPacket != null) {
         ServerPlayer nmsPlayer = CraftBukkitBridge.getServerPlayer(player);
         nmsPlayer.connection.send(nmsPacket);
      }
   }

   private record PacketBundleEntry(Packet<?> packet, List<Player> viewers) {
   }
}
