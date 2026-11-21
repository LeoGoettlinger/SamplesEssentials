package com.viaversion.viaversion.api.protocol.packet;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PacketWrapper {
   int PASSTHROUGH_ID = 1000;

   static PacketWrapper create(@Nullable PacketType packetType, UserConnection connection) {
      return create(packetType, null, connection);
   }

   static PacketWrapper create(@Nullable PacketType packetType, @Nullable ByteBuf inputBuffer, UserConnection connection) {
      return Via.getManager().getProtocolManager().createPacketWrapper(packetType, inputBuffer, connection);
   }

   @Deprecated
   static PacketWrapper create(int packetId, @Nullable ByteBuf inputBuffer, UserConnection connection) {
      return Via.getManager().getProtocolManager().createPacketWrapper(packetId, inputBuffer, connection);
   }

   <T> T get(Type<T> var1, int var2) throws InformativeException;

   @Deprecated
   boolean is(Type var1, int var2);

   boolean isReadable(Type var1, int var2);

   <T> void set(Type<T> var1, int var2, @Nullable T var3) throws InformativeException;

   <T> T read(Type<T> var1) throws InformativeException;

   <T> void write(Type<T> var1, @Nullable T var2);

   <T> T passthrough(Type<T> var1) throws InformativeException;

   <T> T passthroughAndMap(Type<?> var1, Type<T> var2) throws InformativeException;

   void passthroughAll() throws InformativeException;

   void writeToBuffer(ByteBuf var1) throws InformativeException;

   void clearInputBuffer();

   void clearPacket();

   default void send(Class<? extends Protocol> protocol) throws InformativeException {
      this.send(protocol, true);
   }

   void send(Class<? extends Protocol> var1, boolean var2) throws InformativeException;

   default void scheduleSend(Class<? extends Protocol> protocol) throws InformativeException {
      this.scheduleSend(protocol, true);
   }

   void scheduleSend(Class<? extends Protocol> var1, boolean var2) throws InformativeException;

   ChannelFuture sendFuture(Class<? extends Protocol> var1) throws InformativeException;

   void sendRaw() throws InformativeException;

   ChannelFuture sendFutureRaw() throws InformativeException;

   void scheduleSendRaw() throws InformativeException;

   PacketWrapper create(PacketType var1);

   PacketWrapper create(PacketType var1, PacketHandler var2) throws InformativeException;

   PacketWrapper create(int var1);

   PacketWrapper create(int var1, PacketHandler var2) throws InformativeException;

   void apply(Direction var1, State var2, List<Protocol> var3) throws InformativeException, CancelException;

   boolean isCancelled();

   default void cancel() {
      this.setCancelled(true);
   }

   void setCancelled(boolean var1);

   UserConnection user();

   void resetReader();

   void rewindReader(int var1);

   void sendToServerRaw() throws InformativeException;

   void scheduleSendToServerRaw() throws InformativeException;

   default void sendToServer(Class<? extends Protocol> protocol) throws InformativeException {
      this.sendToServer(protocol, true);
   }

   void sendToServer(Class<? extends Protocol> var1, boolean var2) throws InformativeException;

   default void scheduleSendToServer(Class<? extends Protocol> protocol) throws InformativeException {
      this.scheduleSendToServer(protocol, true);
   }

   void scheduleSendToServer(Class<? extends Protocol> var1, boolean var2) throws InformativeException;

   void consumeReadsOnly(Runnable var1);

   @Nullable
   PacketType getPacketType();

   void setPacketType(@Nullable PacketType var1);

   int getId();

   @Deprecated
   void setId(int var1);
}
