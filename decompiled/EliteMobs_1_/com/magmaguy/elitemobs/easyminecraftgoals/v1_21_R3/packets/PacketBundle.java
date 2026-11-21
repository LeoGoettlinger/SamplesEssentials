package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R3.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
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
      Map<Player, List<Packet<PacketListenerPlayOut>>> playerPackets = new HashMap<>();

      for (PacketBundle.PacketBundleEntry entry : this.entries) {
         if (!entry.viewers().isEmpty() && this.isClientGamePacket(entry.packet())) {
            Packet<PacketListenerPlayOut> clientPacket = (Packet<PacketListenerPlayOut>)entry.packet();

            for (Player viewer : entry.viewers()) {
               playerPackets.computeIfAbsent(viewer, k -> new ArrayList<>()).add(clientPacket);
            }
         }
      }

      playerPackets.forEach((player, packets) -> {
         if (!packets.isEmpty() && player != null && player.isOnline()) {
            if (BedrockChecker.isBedrock(player)) {
               for (Packet<PacketListenerPlayOut> p : packets) {
                  this.sendPacketDirect(player, p);
               }
            } else {
               for (int i = 0; i < packets.size(); i += 3000) {
                  int end = Math.min(i + 3000, packets.size());
                  List<Packet<PacketListenerPlayOut>> chunk = packets.subList(i, end);
                  ClientboundBundlePacket bundle = new ClientboundBundlePacket(new HashSet<>(chunk));
                  this.sendPacketBundle(player, bundle);
               }
            }
         }
      });
      int bundleCount = playerPackets.values().stream().mapToInt(packets -> (packets.size() + 3000 - 1) / 3000).sum();
   }

   private boolean isClientGamePacket(Packet<?> packet) {
      return packet != null;
   }

   private void sendPacketBundle(Player player, Packet<?> nmsPacket) {
      if (nmsPacket != null) {
         EntityPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
         nmsPlayer.f.b(nmsPacket);
      }
   }

   private void sendPacketDirect(Player player, Packet<?> nmsPacket) {
      if (nmsPacket != null) {
         EntityPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
         nmsPlayer.f.b(nmsPacket);
      }
   }

   private record PacketBundleEntry(Packet<?> packet, List<Player> viewers) {
   }
}
