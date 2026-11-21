package com.viaversion.viabackwards.protocol.v1_20_2to1_20.storage;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.protocol.v1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ConfigurationPacketStorage implements StorableObject {
   private final List<ConfigurationPacketStorage.QueuedPacket> rawPackets = new ArrayList<>();
   private CompoundTag registry;
   private String[] enabledFeatures;
   private boolean finished;
   private ConfigurationPacketStorage.QueuedPacket resourcePack;

   public void setResourcePack(PacketWrapper wrapper) {
      this.resourcePack = this.toQueuedPacket(wrapper, ClientboundPackets1_19_4.RESOURCE_PACK);
   }

   public CompoundTag registry() {
      Preconditions.checkNotNull(this.registry);
      return this.registry;
   }

   public void setRegistry(CompoundTag registry) {
      this.registry = registry;
   }

   @Nullable
   public String[] enabledFeatures() {
      return this.enabledFeatures;
   }

   public void setEnabledFeatures(String[] enabledFeatures) {
      this.enabledFeatures = enabledFeatures;
   }

   public void addRawPacket(PacketWrapper wrapper, PacketType type) {
      this.rawPackets.add(this.toQueuedPacket(wrapper, type));
   }

   private ConfigurationPacketStorage.QueuedPacket toQueuedPacket(PacketWrapper wrapper, PacketType type) {
      Preconditions.checkArgument(!wrapper.isCancelled(), "Wrapper should be cancelled AFTER calling toQueuedPacket");
      ByteBuf buf = Unpooled.buffer();
      wrapper.setId(-1);
      wrapper.writeToBuffer(buf);
      return new ConfigurationPacketStorage.QueuedPacket(buf, type);
   }

   public void sendQueuedPackets(UserConnection connection) {
      List<ConfigurationPacketStorage.QueuedPacket> packets = this.rawPackets;
      if (this.resourcePack != null) {
         packets = new ArrayList<>(this.rawPackets);
         packets.add(this.resourcePack);
         this.resourcePack = null;
      }

      for (ConfigurationPacketStorage.QueuedPacket queuedPacket : packets) {
         ByteBuf buf = queuedPacket.buf().copy();

         try {
            PacketWrapper packet = PacketWrapper.create(queuedPacket.packetType(), buf, connection);
            packet.send(Protocol1_20_2To1_20.class);
         } finally {
            buf.release();
         }
      }
   }

   public boolean isFinished() {
      return this.finished;
   }

   public void setFinished(boolean finished) {
      this.finished = finished;
   }

   public record QueuedPacket(ByteBuf buf, PacketType packetType) {
   }
}
