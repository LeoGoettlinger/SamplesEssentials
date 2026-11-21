package com.github.retrooper.packetevents.event;

import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PacketSendEvent extends ProtocolPacketEvent {
   @Nullable
   private List<Runnable> tasksAfterSend = null;

   protected PacketSendEvent(Object channel, User user, @UnknownNullability Object player, Object rawByteBuf, boolean autoProtocolTranslation) throws PacketProcessException {
      super(PacketSide.SERVER, channel, user, player, rawByteBuf, autoProtocolTranslation);
   }

   protected PacketSendEvent(
      int packetID, PacketTypeCommon packetType, ServerVersion serverVersion, Object channel, User user, @UnknownNullability Object player, Object byteBuf
   ) throws PacketProcessException {
      super(packetID, packetType, serverVersion, channel, user, player, byteBuf);
   }

   @Override
   public void call(PacketListenerCommon listener) {
      listener.onPacketSend(this);
   }

   public List<Runnable> getTasksAfterSend() {
      if (this.tasksAfterSend == null) {
         this.tasksAfterSend = new ArrayList<>();
      }

      return this.tasksAfterSend;
   }

   public boolean hasTasksAfterSend() {
      return this.tasksAfterSend != null && !this.tasksAfterSend.isEmpty();
   }

   public PacketSendEvent clone() {
      Object clonedBuffer = ByteBufHelper.retainedDuplicate(this.getByteBuf());
      return new PacketSendEvent(
         this.getPacketId(), this.getPacketType(), this.getServerVersion(), this.getChannel(), this.getUser(), this.getPlayer(), clonedBuffer
      );
   }
}
