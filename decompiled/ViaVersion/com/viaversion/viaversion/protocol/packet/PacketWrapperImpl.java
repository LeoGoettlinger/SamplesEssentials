package com.viaversion.viaversion.protocol.packet;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PacketWrapperImpl implements PacketWrapper {
   private final Deque<PacketWrapperImpl.PacketValue<?>> readableObjects = new ArrayDeque<>();
   private final List<PacketWrapperImpl.PacketValue<?>> packetValues = new ArrayList<>();
   private final ByteBuf inputBuffer;
   private final UserConnection userConnection;
   private boolean send = true;
   private PacketType packetType;
   private boolean allActionsRead;
   private int id;

   public PacketWrapperImpl(int packetId, @Nullable ByteBuf inputBuffer, UserConnection userConnection) {
      this.id = packetId;
      this.inputBuffer = inputBuffer;
      this.userConnection = userConnection;
   }

   public PacketWrapperImpl(@Nullable PacketType packetType, @Nullable ByteBuf inputBuffer, UserConnection userConnection) {
      this.packetType = packetType;
      this.id = packetType != null ? packetType.getId() : -1;
      this.inputBuffer = inputBuffer;
      this.userConnection = userConnection;
   }

   @Override
   public <T> T get(Type<T> type, int index) throws InformativeException {
      int currentIndex = 0;

      for (PacketWrapperImpl.PacketValue<?> packetValue : this.packetValues) {
         if (packetValue.type() == type) {
            if (currentIndex == index) {
               return (T)packetValue.value();
            }

            currentIndex++;
         }
      }

      throw this.createInformativeException(new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index), type, index);
   }

   @Override
   public boolean is(Type type, int index) {
      int currentIndex = 0;

      for (PacketWrapperImpl.PacketValue<?> packetValue : this.packetValues) {
         if (packetValue.type() == type) {
            if (currentIndex == index) {
               return true;
            }

            currentIndex++;
         }
      }

      return false;
   }

   @Override
   public boolean isReadable(Type type, int index) {
      int currentIndex = 0;

      for (PacketWrapperImpl.PacketValue<?> packetValue : this.readableObjects) {
         if (packetValue.type().getBaseClass() == type.getBaseClass()) {
            if (currentIndex == index) {
               return true;
            }

            currentIndex++;
         }
      }

      return false;
   }

   @Override
   public <T> void set(Type<T> type, int index, @Nullable T value) throws InformativeException {
      int currentIndex = 0;

      for (PacketWrapperImpl.PacketValue packetValue : this.packetValues) {
         if (packetValue.type() == type) {
            if (currentIndex == index) {
               packetValue.setValue(value);
               return;
            }

            currentIndex++;
         }
      }

      throw this.createInformativeException(new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index), type, index);
   }

   @Override
   public <T> T read(Type<T> type) {
      return this.readableObjects.isEmpty() ? this.readFromBuffer(type) : this.pollReadableObject(type).value;
   }

   private <T> T readFromBuffer(Type<T> type) {
      Preconditions.checkNotNull(this.inputBuffer, "This packet does not have an input buffer.");

      try {
         return type.read(this.inputBuffer);
      } catch (Exception var3) {
         throw this.createInformativeException(var3, type, this.packetValues.size() + 1);
      }
   }

   private <T> PacketWrapperImpl.PacketValue<T> pollReadableObject(Type<T> type) {
      PacketWrapperImpl.PacketValue<?> readValue = this.readableObjects.poll();
      Type<?> readType = readValue.type();
      if (readType != type && (type.getBaseClass() != readType.getBaseClass() || type.getOutputClass() != readType.getOutputClass())) {
         throw this.createInformativeException(
            new IOException("Unable to read type " + type.getTypeName() + ", found " + readValue.type().getTypeName()), type, this.readableObjects.size()
         );
      } else {
         return (PacketWrapperImpl.PacketValue<T>)readValue;
      }
   }

   @Override
   public <T> void write(Type<T> type, T value) {
      this.addPacketValue(new PacketWrapperImpl.PacketValue<>(type, value));
   }

   @Nullable
   private <T> T attemptTransform(Type<T> expectedType, @Nullable Object value) {
      if (value != null && !expectedType.getOutputClass().isAssignableFrom(value.getClass())) {
         if (expectedType instanceof TypeConverter) {
            return ((TypeConverter)expectedType).from(value);
         }

         Via.getPlatform().getLogger().warning("Possible type mismatch: " + value.getClass().getName() + " -> " + expectedType.getOutputClass());
      }

      return (T)value;
   }

   @Override
   public <T> T passthrough(Type<T> type) throws InformativeException {
      if (this.readableObjects.isEmpty()) {
         T value = this.readFromBuffer(type);
         this.addPacketValue(new PacketWrapperImpl.PacketValue<>(type, value));
         return value;
      } else {
         PacketWrapperImpl.PacketValue<T> value = this.pollReadableObject(type);
         this.addPacketValue(value);
         return value.value;
      }
   }

   private void addPacketValue(PacketWrapperImpl.PacketValue<?> packetValue) {
      if (!this.allActionsRead) {
         this.packetValues.add(packetValue);
      }
   }

   @Override
   public <T> T passthroughAndMap(Type<?> type, Type<T> mappedType) throws InformativeException {
      if (type == mappedType) {
         return this.passthrough(mappedType);
      } else {
         Object value = this.read(type);
         T mappedValue = this.attemptTransform(mappedType, value);
         this.write(mappedType, mappedValue);
         return mappedValue;
      }
   }

   @Override
   public void passthroughAll() throws InformativeException {
      this.packetValues.addAll(this.readableObjects);
      this.readableObjects.clear();
      if (this.inputBuffer.isReadable()) {
         this.passthrough(Types.REMAINING_BYTES);
      }
   }

   @Override
   public void writeToBuffer(ByteBuf buffer) throws InformativeException {
      this.writeProcessedValues(buffer);
      if (this.inputBuffer != null) {
         buffer.writeBytes(this.inputBuffer);
      }
   }

   public boolean areStoredPacketValuesEmpty() {
      return this.packetValues.isEmpty() && this.readableObjects.isEmpty();
   }

   public void writeProcessedValues(ByteBuf buffer) throws InformativeException {
      if (this.id != -1) {
         Types.VAR_INT.writePrimitive(buffer, this.id);
      }

      if (!this.readableObjects.isEmpty()) {
         this.packetValues.addAll(this.readableObjects);
         this.readableObjects.clear();
      }

      for (int i = 0; i < this.packetValues.size(); i++) {
         PacketWrapperImpl.PacketValue<?> packetValue = this.packetValues.get(i);

         try {
            packetValue.write(buffer);
         } catch (Exception var5) {
            throw this.createInformativeException(var5, packetValue.type(), i);
         }
      }
   }

   private InformativeException createInformativeException(Exception cause, Type<?> type, int index) {
      ProtocolInfo protocolInfo = this.user().getProtocolInfo();
      return new InformativeException(cause)
         .set("Packet Type", this.packetType)
         .set("Client Protocol", protocolInfo.protocolVersion().getName())
         .set("Server Protocol", protocolInfo.serverProtocolVersion().getName())
         .set("Index", index)
         .set("Type", type.getTypeName())
         .set("Data", this.packetValues)
         .set("Packet ID", this.id);
   }

   @Override
   public void clearInputBuffer() {
      if (this.inputBuffer != null) {
         this.inputBuffer.clear();
      }

      this.readableObjects.clear();
   }

   @Override
   public void clearPacket() {
      this.clearInputBuffer();
      this.packetValues.clear();
   }

   @Override
   public void send(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException {
      this.send0(protocol, skipCurrentPipeline, true);
   }

   @Override
   public void scheduleSend(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException {
      this.send0(protocol, skipCurrentPipeline, false);
   }

   private void send0(Class<? extends Protocol> protocol, boolean skipCurrentPipeline, boolean currentThread) throws InformativeException {
      if (!this.isCancelled()) {
         UserConnection connection = this.user();
         if (currentThread) {
            this.sendNow(protocol, skipCurrentPipeline);
         } else {
            connection.getChannel().eventLoop().execute(() -> this.sendNow(protocol, skipCurrentPipeline));
         }
      }
   }

   private void sendNow(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException {
      try {
         ByteBuf output = this.constructPacket(protocol, skipCurrentPipeline, Direction.CLIENTBOUND);
         this.user().sendRawPacket(output);
      } catch (InformativeException var4) {
         throw var4;
      } catch (CancelException var5) {
      } catch (Exception var6) {
         if (!PipelineUtil.containsCause(var6, CancelException.class)) {
            throw new InformativeException(var6);
         }
      }
   }

   private ByteBuf constructPacket(@Nullable Class<? extends Protocol> protocolClass, boolean skipCurrentPipeline, Direction direction) throws InformativeException, CancelException {
      this.resetReader();
      ProtocolInfo protocolInfo = this.user().getProtocolInfo();
      List<Protocol> protocols = protocolInfo.getPipeline().pipes(protocolClass, skipCurrentPipeline, direction);
      State state = this.packetType != null ? this.packetType.state() : protocolInfo.getState(direction);
      this.apply(direction, state, protocols);
      ByteBuf output = this.allocateOutputBuffer();

      ByteBuf var8;
      try {
         this.writeToBuffer(output);
         var8 = output.retain();
      } finally {
         output.release();
      }

      return var8;
   }

   @Override
   public ChannelFuture sendFuture(Class<? extends Protocol> protocolClass) throws InformativeException {
      if (!this.isCancelled()) {
         ByteBuf output;
         try {
            output = this.constructPacket(protocolClass, true, Direction.CLIENTBOUND);
         } catch (CancelException var4) {
            return this.user().getChannel().newFailedFuture(new RuntimeException("Cancelled packet"));
         }

         return this.user().sendRawPacketFuture(output);
      } else {
         return this.cancelledFuture();
      }
   }

   @Override
   public void sendRaw() throws InformativeException {
      this.sendRaw(true);
   }

   @Override
   public ChannelFuture sendFutureRaw() throws InformativeException {
      if (this.isCancelled()) {
         return this.cancelledFuture();
      } else {
         ByteBuf output = this.allocateOutputBuffer();

         ChannelFuture var2;
         try {
            this.writeToBuffer(output);
            var2 = this.user().sendRawPacketFuture(output.retain());
         } finally {
            output.release();
         }

         return var2;
      }
   }

   @Override
   public void scheduleSendRaw() throws InformativeException {
      this.sendRaw(false);
   }

   private void sendRaw(boolean currentThread) throws InformativeException {
      if (!this.isCancelled()) {
         ByteBuf output = this.allocateOutputBuffer();

         try {
            this.writeToBuffer(output);
            if (currentThread) {
               this.user().sendRawPacket(output.retain());
            } else {
               this.user().scheduleSendRawPacket(output.retain());
            }
         } finally {
            output.release();
         }
      }
   }

   private ByteBuf allocateOutputBuffer() {
      return this.inputBuffer == null
         ? this.user().getChannel().alloc().buffer()
         : this.inputBuffer.alloc().buffer(Math.max(this.inputBuffer.readableBytes(), 256));
   }

   private ChannelFuture cancelledFuture() {
      return this.user().getChannel().newFailedFuture(new RuntimeException("Tried to send cancelled packet"));
   }

   public PacketWrapperImpl create(int packetId) {
      return new PacketWrapperImpl(packetId, null, this.user());
   }

   public PacketWrapperImpl create(int packetId, PacketHandler handler) throws InformativeException {
      PacketWrapperImpl wrapper = this.create(packetId);
      handler.handle(wrapper);
      return wrapper;
   }

   public PacketWrapperImpl create(PacketType packetType) {
      return new PacketWrapperImpl(packetType, null, this.user());
   }

   public PacketWrapperImpl create(PacketType packetType, PacketHandler handler) throws InformativeException {
      PacketWrapperImpl wrapper = this.create(packetType);
      handler.handle(wrapper);
      return wrapper;
   }

   @Override
   public void apply(Direction direction, State state, List<Protocol> pipeline) throws InformativeException, CancelException {
      int i = 0;

      for (int size = pipeline.size(); i < size; i++) {
         Protocol<?, ?, ?, ?> protocol = pipeline.get(i);
         protocol.transform(direction, state, this);
         this.resetReader();
         if (this.packetType != null) {
            state = this.packetType.state();
         }
      }
   }

   @Override
   public boolean isCancelled() {
      return !this.send;
   }

   @Override
   public void setCancelled(boolean cancel) {
      this.send = !cancel;
   }

   @Override
   public UserConnection user() {
      return this.userConnection;
   }

   @Override
   public void resetReader() {
      for (int i = this.packetValues.size() - 1; i >= 0; i--) {
         this.readableObjects.addFirst(this.packetValues.get(i));
      }

      this.packetValues.clear();
   }

   @Override
   public void rewindReader(int values) {
      if (!this.allActionsRead) {
         int size = this.packetValues.size();
         Preconditions.checkArgument(values <= size, "Tried resetting more values than there are readable values");

         for (int i = size - 1; i >= size - values; i--) {
            this.readableObjects.addFirst(this.packetValues.remove(i));
         }
      }
   }

   @Override
   public void sendToServerRaw() throws InformativeException {
      this.sendToServerRaw(true);
   }

   @Override
   public void scheduleSendToServerRaw() throws InformativeException {
      this.sendToServerRaw(false);
   }

   private void sendToServerRaw(boolean currentThread) throws InformativeException {
      if (!this.isCancelled()) {
         ByteBuf output = this.allocateOutputBuffer();

         try {
            this.writeToBuffer(output);
            if (currentThread) {
               this.user().sendRawPacketToServer(output.retain());
            } else {
               this.user().scheduleSendRawPacketToServer(output.retain());
            }
         } finally {
            output.release();
         }
      }
   }

   @Override
   public void sendToServer(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException {
      this.sendToServer0(protocol, skipCurrentPipeline, true);
   }

   @Override
   public void scheduleSendToServer(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException {
      this.sendToServer0(protocol, skipCurrentPipeline, false);
   }

   private void sendToServer0(Class<? extends Protocol> protocol, boolean skipCurrentPipeline, boolean currentThread) throws InformativeException {
      if (!this.isCancelled()) {
         UserConnection connection = this.user();
         if (currentThread) {
            try {
               ByteBuf output = this.constructPacket(protocol, skipCurrentPipeline, Direction.SERVERBOUND);
               connection.sendRawPacketToServer(output);
            } catch (InformativeException var6) {
               throw var6;
            } catch (CancelException var7) {
            } catch (Exception var8) {
               if (!PipelineUtil.containsCause(var8, CancelException.class)) {
                  throw new InformativeException(var8);
               }
            }
         } else {
            connection.getChannel().eventLoop().execute(() -> {
               try {
                  ByteBuf output = this.constructPacket(protocol, skipCurrentPipeline, Direction.SERVERBOUND);
                  connection.sendRawPacketToServer(output);
               } catch (InformativeException var5x) {
                  throw var5x;
               } catch (CancelException var6x) {
               } catch (Exception var7x) {
                  if (!PipelineUtil.containsCause(var7x, CancelException.class)) {
                     throw new InformativeException(var7x);
                  }
               }
            });
         }
      }
   }

   @Nullable
   @Override
   public PacketType getPacketType() {
      return this.packetType;
   }

   @Override
   public void setPacketType(PacketType packetType) {
      this.packetType = packetType;
      this.id = packetType != null ? packetType.getId() : -1;
   }

   @Override
   public int getId() {
      return this.id;
   }

   @Deprecated
   @Override
   public void setId(int id) {
      this.packetType = null;
      this.id = id;
   }

   @Nullable
   public ByteBuf getInputBuffer() {
      return this.inputBuffer;
   }

   public void setAllActionsRead(boolean allActionsRead) {
      this.allActionsRead = allActionsRead;
   }

   @Override
   public void consumeReadsOnly(Runnable runnable) {
      boolean previous = this.allActionsRead;
      this.allActionsRead = true;

      try {
         runnable.run();
      } finally {
         this.allActionsRead = previous;
      }
   }

   @Override
   public String toString() {
      return "PacketWrapper{type=" + this.packetType + ", id=" + this.id + ", values=" + this.packetValues + ", readable=" + this.readableObjects + "}";
   }

   public static final class PacketValue<T> {
      private final Type<T> type;
      private T value;

      private PacketValue(Type<T> type, @Nullable T value) {
         this.type = type;
         this.value = value;
      }

      public Type<T> type() {
         return this.type;
      }

      @Nullable
      public Object value() {
         return this.value;
      }

      public void write(ByteBuf buffer) throws Exception {
         this.type.write(buffer, this.value);
      }

      public void setValue(@Nullable T value) {
         this.value = value;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            PacketWrapperImpl.PacketValue<?> that = (PacketWrapperImpl.PacketValue<?>)o;
            return !this.type.equals(that.type) ? false : Objects.equals(this.value, that.value);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.type.hashCode();
         return 31 * result + (this.value != null ? this.value.hashCode() : 0);
      }

      @Override
      public String toString() {
         return "{" + this.type + ": " + ArrayUtil.toString(this.value) + "}";
      }
   }
}
