package com.viaversion.viaversion.api.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.exception.InformativeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.CodecException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UserConnection {
   @Nullable
   <T extends StorableObject> T get(Class<T> var1);

   boolean has(Class<? extends StorableObject> var1);

   @Nullable
   <T extends StorableObject> T remove(Class<T> var1);

   void put(StorableObject var1);

   <T extends ProtocolStorables> T storables(Protocol<?, ?, ?, ?> var1);

   default <T extends ProtocolStorables> T storables(Class<? extends Protocol> protocolClass) {
      return this.storables(Via.getManager().getProtocolManager().getProtocol((Class<Protocol<?, ?, ?, ?>>)protocolClass));
   }

   @Deprecated(
      forRemoval = true
   )
   Collection<EntityTracker> getEntityTrackers();

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   <T extends EntityTracker> T getEntityTracker(Class<? extends Protocol> var1);

   @Nullable
   <T extends EntityTracker> T getEntityTracker(Protocol<?, ?, ?, ?> var1);

   @Deprecated(
      forRemoval = true
   )
   void addEntityTracker(Class<? extends Protocol> var1, EntityTracker var2);

   @Nullable
   <T extends ItemHasher> T getItemHasher(Protocol<?, ?, ?, ?> var1);

   @Deprecated(
      forRemoval = true
   )
   void addItemHasher(Class<? extends Protocol> var1, ItemHasher var2);

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   <T extends ItemHasher> T getItemHasher(Class<? extends Protocol> var1);

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   <T extends ClientWorld> T getClientWorld(Class<? extends Protocol> var1);

   @Deprecated(
      forRemoval = true
   )
   void addClientWorld(Class<? extends Protocol> var1, ClientWorld var2);

   void clearStoredObjects();

   void sendRawPacket(ByteBuf var1);

   void scheduleSendRawPacket(ByteBuf var1);

   ChannelFuture sendRawPacketFuture(ByteBuf var1);

   PacketTracker getPacketTracker();

   void disconnect(String var1);

   void sendRawPacketToServer(ByteBuf var1);

   void scheduleSendRawPacketToServer(ByteBuf var1);

   boolean checkServerboundPacket(int var1);

   @Deprecated(
      forRemoval = true
   )
   default boolean checkServerboundPacket() {
      return this.checkServerboundPacket(0);
   }

   boolean checkClientboundPacket();

   default boolean checkIncomingPacket(int bytes) {
      return this.isClientSide() ? this.checkClientboundPacket() : this.checkServerboundPacket(bytes);
   }

   @Deprecated(
      forRemoval = true
   )
   default boolean checkIncomingPacket() {
      return this.isClientSide() ? this.checkClientboundPacket() : this.checkServerboundPacket();
   }

   default boolean checkOutgoingPacket() {
      return this.isClientSide() ? this.checkServerboundPacket() : this.checkClientboundPacket();
   }

   boolean shouldTransformPacket();

   void transformClientbound(ByteBuf var1, Function<Throwable, CodecException> var2) throws InformativeException;

   void transformServerbound(ByteBuf var1, Function<Throwable, CodecException> var2) throws InformativeException;

   default void transformOutgoing(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException {
      if (this.isClientSide()) {
         this.transformServerbound(buf, cancelSupplier);
      } else {
         this.transformClientbound(buf, cancelSupplier);
      }
   }

   default void transformIncoming(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException {
      if (this.isClientSide()) {
         this.transformClientbound(buf, cancelSupplier);
      } else {
         this.transformServerbound(buf, cancelSupplier);
      }
   }

   long getId();

   @Nullable
   Channel getChannel();

   ProtocolInfo getProtocolInfo();

   Map<Class<?>, StorableObject> getStoredObjects();

   boolean isActive();

   void setActive(boolean var1);

   boolean isPendingDisconnect();

   void setPendingDisconnect(boolean var1);

   boolean isClientSide();

   default boolean isServerSide() {
      return !this.isClientSide();
   }

   default boolean shouldApplyBlockProtocol() {
      return this.isServerSide();
   }

   long generatePassthroughToken();
}
