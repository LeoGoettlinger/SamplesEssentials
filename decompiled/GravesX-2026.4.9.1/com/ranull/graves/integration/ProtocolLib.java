package com.ranull.graves.integration;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.ranull.graves.Graves;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ProtocolLib {
   private final Graves plugin;
   private final ProtocolManager protocolManager;

   public ProtocolLib(Graves plugin) {
      this.plugin = plugin;
      this.protocolManager = ProtocolLibrary.getProtocolManager();
   }

   public void setBlock(Block block, Material material, Player player) throws InvocationTargetException {
      if (block != null && material != null && player != null) {
         block.getWorld();
         if (Objects.equals(block.getWorld(), player.getWorld())) {
            WrappedBlockData wrapped = WrappedBlockData.createData(material);
            this.sendServerPacket(player, this.createBlockChangePacket(block, wrapped));
         }
      }
   }

   public void refreshBlock(Block block, Player player) throws InvocationTargetException {
      if (block != null && player != null) {
         block.getWorld();
         if (Objects.equals(block.getWorld(), player.getWorld())) {
            WrappedBlockData current = WrappedBlockData.createData(block.getBlockData());
            this.sendServerPacket(player, this.createBlockChangePacket(block, current));
         }
      }
   }

   private PacketContainer createBlockChangePacket(Block block, WrappedBlockData wrappedBlockData) {
      Location loc = block.getLocation();
      BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
      PacketContainer packet = this.protocolManager.createPacket(Server.BLOCK_CHANGE);
      packet.getBlockPositionModifier().write(0, pos);
      packet.getBlockData().write(0, wrappedBlockData);
      return packet;
   }

   private void sendServerPacket(Player player, PacketContainer packetContainer) throws InvocationTargetException {
      try {
         this.protocolManager.sendServerPacket(player, packetContainer);
      } catch (Exception var4) {
         if (this.plugin != null) {
            this.plugin.getLogger().warning("Failed to send BLOCK_CHANGE packet: " + var4.getMessage());
         }
      }
   }
}
