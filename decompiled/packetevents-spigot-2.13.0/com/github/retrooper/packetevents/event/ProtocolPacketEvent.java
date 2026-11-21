package com.github.retrooper.packetevents.event;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.exception.InvalidDisconnectPacketSend;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class ProtocolPacketEvent extends PacketEvent implements PlayerEvent, CancellableEvent, UserEvent {
   private final int packetID;
   private final PacketTypeCommon packetType;
   private ServerVersion serverVersion;
   private final Object channel;
   private final ConnectionState connectionState;
   private final User user;
   @UnknownNullability
   private Object player;
   private Object byteBuf;
   private boolean cancel;
   @Nullable
   private PacketWrapper<?> lastUsedWrapper;
   @Nullable
   private List<Runnable> postTasks = null;
   private boolean cloned;
   private boolean needsReEncode = PacketEvents.getAPI().getSettings().reEncodeByDefault();

   public ProtocolPacketEvent(
      PacketSide packetSide, Object channel, User user, @UnknownNullability Object player, Object byteBuf, boolean autoProtocolTranslation
   ) throws PacketProcessException {
      this.channel = channel;
      this.user = user;
      this.player = player;
      if (!autoProtocolTranslation && user.getClientVersion() != null) {
         this.serverVersion = user.getPacketVersion().toServerVersion();
      } else {
         this.serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
      }

      this.byteBuf = byteBuf;
      int size = ByteBufHelper.readableBytes(byteBuf);
      if (size == 0) {
         throw new PacketProcessException("Trying to process a packet, but it has no content. (Size=0)");
      } else {
         try {
            this.packetID = ByteBufHelper.readVarInt(byteBuf);
         } catch (Exception var10) {
            throw new PacketProcessException("Failed to read the Packet ID of a packet. (Size: " + size + ")");
         }

         ClientVersion version = this.serverVersion.toClientVersion();
         this.connectionState = packetSide == PacketSide.CLIENT ? user.getDecoderState() : user.getEncoderState();
         PacketTypeCommon packetType = PacketType.getById(packetSide, this.connectionState, version, this.packetID);
         if (packetType == null) {
            if (PacketType.getById(packetSide, ConnectionState.PLAY, version, this.packetID) == PacketType.Play.Server.DISCONNECT) {
               throw new InvalidDisconnectPacketSend();
            } else {
               throw new PacketProcessException(
                  "Failed to map the Packet ID "
                     + this.packetID
                     + " to a PacketType constant. Bound: "
                     + packetSide.getOpposite()
                     + ", Connection state: "
                     + user.getDecoderState()
                     + ", Server version: "
                     + this.serverVersion.getReleaseName()
               );
            }
         } else {
            this.packetType = packetType;
         }
      }
   }

   public ProtocolPacketEvent(
      int packetID, PacketTypeCommon packetType, ServerVersion serverVersion, Object channel, User user, @UnknownNullability Object player, Object byteBuf
   ) {
      this.channel = channel;
      this.user = user;
      this.player = player;
      this.serverVersion = serverVersion;
      this.byteBuf = byteBuf;
      this.packetID = packetID;
      this.packetType = packetType;
      this.connectionState = packetType != null && packetType.getSide() == PacketSide.SERVER ? user.getEncoderState() : user.getDecoderState();
      this.cloned = true;
   }

   public void markForReEncode(boolean needsReEncode) {
      this.needsReEncode = needsReEncode;
   }

   public boolean needsReEncode() {
      return this.needsReEncode;
   }

   public boolean isClone() {
      return this.cloned;
   }

   public Object getChannel() {
      return this.channel;
   }

   public SocketAddress getAddress() {
      return ChannelHelper.remoteAddress(this.channel);
   }

   public InetSocketAddress getSocketAddress() {
      return (InetSocketAddress)this.getAddress();
   }

   @Override
   public User getUser() {
      return this.user;
   }

   @UnknownNullability
   @Override
   public <T> T getPlayer() {
      return (T)this.player;
   }

   @Deprecated
   @Internal
   public void setPlayer(Object player) {
      this.player = player;
   }

   public ConnectionState getConnectionState() {
      return this.connectionState;
   }

   @Deprecated
   public ClientVersion getClientVersion() {
      return this.user.getClientVersion();
   }

   @Deprecated
   public void setClientVersion(ClientVersion clientVersion) {
      PacketEvents.getAPI().getLogManager().debug("Setting client version with deprecated method " + clientVersion.getReleaseName());
      this.user.setClientVersion(clientVersion);
   }

   public ServerVersion getServerVersion() {
      return this.serverVersion;
   }

   @Deprecated
   public void setServerVersion(ServerVersion serverVersion) {
      this.serverVersion = serverVersion;
   }

   public Object getByteBuf() {
      return this.byteBuf;
   }

   public void setByteBuf(Object byteBuf) {
      this.byteBuf = byteBuf;
   }

   public int getPacketId() {
      return this.packetID;
   }

   public PacketTypeCommon getPacketType() {
      return this.packetType;
   }

   @Deprecated
   public String getPacketName() {
      return ((Enum)this.packetType).name();
   }

   @Override
   public boolean isCancelled() {
      return this.cancel;
   }

   @Override
   public void setCancelled(boolean val) {
      this.cancel = val;
   }

   @Nullable
   public PacketWrapper<?> getLastUsedWrapper() {
      return this.lastUsedWrapper;
   }

   public void setLastUsedWrapper(@Nullable PacketWrapper<?> lastUsedWrapper) {
      this.lastUsedWrapper = lastUsedWrapper;
   }

   public List<Runnable> getPostTasks() {
      if (this.postTasks == null) {
         this.postTasks = new ArrayList<>();
      }

      return this.postTasks;
   }

   public boolean hasPostTasks() {
      return this.postTasks != null && !this.postTasks.isEmpty();
   }

   public ProtocolPacketEvent clone() {
      return (ProtocolPacketEvent)(this instanceof PacketReceiveEvent ? ((PacketReceiveEvent)this).clone() : ((PacketSendEvent)this).clone());
   }

   public void cleanUp() {
      if (this.isClone()) {
         ByteBufHelper.release(this.byteBuf);
      }
   }

   public Object getFullBufferClone() {
      byte[] data = ByteBufHelper.copyBytes(this.getByteBuf());
      Object buffer = UnpooledByteBufAllocationHelper.buffer();
      ByteBufHelper.writeVarInt(buffer, this.getPacketId());
      ByteBufHelper.writeBytes(buffer, data);
      return buffer;
   }
}
