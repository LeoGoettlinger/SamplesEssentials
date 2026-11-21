package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.ProtocolStorables;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.VarIntType;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.libs.fastutil.longs.LongOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.longs.LongSet;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.CodecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UserConnectionImpl implements UserConnection {
   private static final int PASSTHROUGH_DATA_BYTES = 10;
   private static final AtomicLong IDS = new AtomicLong();
   private final long id = IDS.incrementAndGet();
   private final SplittableRandom random = new SplittableRandom();
   private final Map<Class<?>, StorableObject> storedObjects = new ConcurrentHashMap<>(8);
   private final ProtocolStorables[] protocolStorables = new ProtocolStorables[Via.getManager().getProtocolManager().registeredProtocolCount()];
   private final PacketTracker packetTracker = new PacketTracker(this);
   private final LongSet passthroughTokens = new LongOpenHashSet(4);
   private final ProtocolInfo protocolInfo = new ProtocolInfoImpl();
   private final Channel channel;
   private final boolean clientSide;
   private boolean active = true;
   private boolean pendingDisconnect;

   public UserConnectionImpl(@Nullable Channel channel, boolean clientSide) {
      this.channel = channel;
      this.clientSide = clientSide;
   }

   public UserConnectionImpl(@Nullable Channel channel) {
      this(channel, false);
   }

   @Nullable
   @Override
   public <T extends StorableObject> T get(Class<T> objectClass) {
      return (T)this.storedObjects.get(objectClass);
   }

   @Override
   public boolean has(Class<? extends StorableObject> objectClass) {
      return this.storedObjects.containsKey(objectClass);
   }

   @Nullable
   @Override
   public <T extends StorableObject> T remove(Class<T> objectClass) {
      StorableObject object = this.storedObjects.remove(objectClass);
      if (object != null) {
         object.onRemove();
      }

      return (T)object;
   }

   @Override
   public void put(StorableObject object) {
      StorableObject previousObject = this.storedObjects.put(object.getClass(), object);
      if (previousObject != null) {
         previousObject.onRemove();
      }
   }

   @Override
   public Collection<EntityTracker> getEntityTrackers() {
      List<EntityTracker> trackers = new ArrayList<>();

      for (ProtocolStorables storable : this.protocolStorables) {
         if (storable != null && storable.entityTracker() != null) {
            trackers.add(storable.entityTracker());
         }
      }

      return trackers;
   }

   @Nullable
   @Override
   public <T extends EntityTracker> T getEntityTracker(Class<? extends Protocol> protocolClass) {
      ProtocolStorables storables = this.storablesByClassIfPresent(protocolClass);
      return (T)(storables != null ? storables.entityTracker() : null);
   }

   @Override
   public void addEntityTracker(Class<? extends Protocol> protocolClass, EntityTracker tracker) {
      this.<ProtocolStorables>storables(protocolClass).setEntityTracker(tracker);
   }

   @Override
   public void addItemHasher(Class<? extends Protocol> protocolClass, ItemHasher itemHasher) {
      this.<ProtocolStorables>storables(protocolClass).setItemHasher(itemHasher);
   }

   @Nullable
   @Override
   public <T extends ItemHasher> T getItemHasher(Class<? extends Protocol> protocolClass) {
      ProtocolStorables storables = this.storablesByClassIfPresent(protocolClass);
      return (T)(storables != null ? storables.itemHasher() : null);
   }

   @Nullable
   @Override
   public <T extends ClientWorld> T getClientWorld(Class<? extends Protocol> protocolClass) {
      ProtocolStorables storables = this.storablesByClassIfPresent(protocolClass);
      return (T)(storables != null ? storables.clientWorld() : null);
   }

   @Override
   public void addClientWorld(Class<? extends Protocol> protocolClass, ClientWorld clientWorld) {
      this.<ProtocolStorables>storables(protocolClass).setClientWorld(clientWorld);
   }

   @Nullable
   private ProtocolStorables storablesByClassIfPresent(Class<? extends Protocol> protocolClass) {
      int index = protocolIndex(protocolClass);
      return index >= 0 && index < this.protocolStorables.length ? this.protocolStorables[index] : null;
   }

   private static int protocolIndex(Class<? extends Protocol> protocolClass) {
      Protocol<?, ?, ?, ?> protocol = Via.getManager().getProtocolManager().getProtocol((Class<Protocol<?, ?, ?, ?>>)protocolClass);
      return protocol != null ? protocol.index() : -1;
   }

   @Nullable
   @Override
   public <T extends EntityTracker> T getEntityTracker(Protocol<?, ?, ?, ?> protocol) {
      ProtocolStorables storables = this.storablesIfPresent(protocol);
      return (T)(storables != null ? storables.entityTracker() : null);
   }

   @Nullable
   @Override
   public <T extends ItemHasher> T getItemHasher(Protocol<?, ?, ?, ?> protocol) {
      ProtocolStorables storables = this.storablesIfPresent(protocol);
      return (T)(storables != null ? storables.itemHasher() : null);
   }

   @Override
   public <T extends ProtocolStorables> T storables(Protocol<?, ?, ?, ?> protocol) {
      T storables = this.storablesIfPresent(protocol);
      if (storables == null) {
         storables = (T)protocol.createStorables();
         this.protocolStorables[protocol.index()] = storables;
      }

      return storables;
   }

   public <T extends ProtocolStorables> T storablesIfPresent(Protocol<?, ?, ?, ?> protocol) {
      int index = protocol.index();
      if (index == -1) {
         throw new IllegalArgumentException("Provided protocol does not have a valid index: " + index);
      } else {
         return (T)this.protocolStorables[index];
      }
   }

   @Override
   public void clearStoredObjects() {
      for (StorableObject object : this.storedObjects.values()) {
         object.onRemove();
      }

      this.storedObjects.clear();
      Arrays.fill(this.protocolStorables, null);
   }

   @Override
   public void sendRawPacket(ByteBuf packet) {
      this.sendRawPacket(packet, true);
   }

   @Override
   public void scheduleSendRawPacket(ByteBuf packet) {
      this.sendRawPacket(packet, false);
   }

   private void sendRawPacket(ByteBuf packet, boolean currentThread) {
      if (currentThread) {
         this.sendRawPacketNow(packet);
      } else {
         try {
            this.channel.eventLoop().execute(() -> this.sendRawPacketNow(packet));
         } catch (Throwable var4) {
            packet.release();
            var4.printStackTrace();
         }
      }
   }

   private void sendRawPacketNow(ByteBuf buf) {
      ChannelPipeline pipeline = this.getChannel().pipeline();
      ViaInjector injector = Via.getManager().getInjector();
      if (this.clientSide) {
         pipeline.context(injector.getDecoderName()).fireChannelRead(buf);
      } else {
         pipeline.context(injector.getEncoderName()).writeAndFlush(buf);
      }
   }

   @Override
   public ChannelFuture sendRawPacketFuture(ByteBuf packet) {
      if (this.clientSide) {
         this.getChannel().pipeline().context(Via.getManager().getInjector().getDecoderName()).fireChannelRead(packet);
         return this.getChannel().newSucceededFuture();
      } else {
         return this.channel.pipeline().context(Via.getManager().getInjector().getEncoderName()).writeAndFlush(packet);
      }
   }

   @Override
   public PacketTracker getPacketTracker() {
      return this.packetTracker;
   }

   @Override
   public void disconnect(String reason) {
      if (this.channel.isOpen() && !this.pendingDisconnect) {
         this.pendingDisconnect = true;
         if (this.isServerSide()) {
            Via.getPlatform().runSync(() -> {
               if (!Via.getPlatform().kickPlayer(this, ChatColorUtil.translateAlternateColorCodes(reason))) {
                  this.channel.close();
               }
            });
         } else {
            this.channel.close();
         }
      }
   }

   @Override
   public void sendRawPacketToServer(ByteBuf packet) {
      if (this.clientSide) {
         this.sendRawPacketToServerClientSide(packet, true);
      } else {
         this.sendRawPacketToServerServerSide(packet, true);
      }
   }

   @Override
   public void scheduleSendRawPacketToServer(ByteBuf packet) {
      if (this.clientSide) {
         this.sendRawPacketToServerClientSide(packet, false);
      } else {
         this.sendRawPacketToServerServerSide(packet, false);
      }
   }

   private void sendRawPacketToServerServerSide(ByteBuf packet, boolean currentThread) {
      int initialCapacity = this.active ? packet.readableBytes() + 10 : packet.readableBytes();
      ByteBuf buf = packet.alloc().buffer(initialCapacity);

      try {
         ChannelHandlerContext context = PipelineUtil.getPreviousContext(Via.getManager().getInjector().getDecoderName(), this.channel.pipeline());
         if (this.shouldTransformPacket()) {
            Types.VAR_INT.writePrimitive(buf, 1000);
            Types.LONG.writePrimitive(buf, this.generatePassthroughToken());
         }

         buf.writeBytes(packet);
         if (currentThread) {
            this.fireChannelRead(context, buf);
         } else {
            try {
               this.channel.eventLoop().execute(() -> this.fireChannelRead(context, buf));
            } catch (Throwable var10) {
               buf.release();
               throw var10;
            }
         }
      } finally {
         packet.release();
      }
   }

   private void fireChannelRead(@Nullable ChannelHandlerContext context, ByteBuf buf) {
      if (context != null) {
         context.fireChannelRead(buf);
      } else {
         this.channel.pipeline().fireChannelRead(buf);
      }
   }

   private void sendRawPacketToServerClientSide(ByteBuf packet, boolean currentThread) {
      if (currentThread) {
         this.writeAndFlush(packet);
      } else {
         try {
            this.getChannel().eventLoop().execute(() -> this.writeAndFlush(packet));
         } catch (Throwable var4) {
            var4.printStackTrace();
            packet.release();
         }
      }
   }

   private void writeAndFlush(ByteBuf buf) {
      this.getChannel().pipeline().context(Via.getManager().getInjector().getEncoderName()).writeAndFlush(buf);
   }

   @Override
   public boolean checkServerboundPacket(int bytes) {
      if (this.pendingDisconnect) {
         return false;
      } else if (!this.packetTracker.isPacketLimiterEnabled()) {
         return true;
      } else {
         this.packetTracker.incrementReceived(bytes);
         return !this.packetTracker.exceedsLimits();
      }
   }

   @Override
   public boolean checkClientboundPacket() {
      this.packetTracker.incrementSent();
      return true;
   }

   @Override
   public boolean shouldTransformPacket() {
      return this.active;
   }

   @Override
   public void transformClientbound(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException, CodecException {
      this.transform(buf, Direction.CLIENTBOUND, cancelSupplier);
   }

   @Override
   public void transformServerbound(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException, CodecException {
      this.transform(buf, Direction.SERVERBOUND, cancelSupplier);
   }

   private void transform(ByteBuf buf, Direction direction, Function<Throwable, CodecException> cancelSupplier) throws InformativeException, CodecException {
      if (buf.isReadable()) {
         int id = Types.VAR_INT.readPrimitive(buf);
         if (id == 1000 && buf.readableBytes() >= 8) {
            if (this.removePassthroughToken(buf.readLong())) {
               return;
            }

            buf.readerIndex(buf.readerIndex() - 8);
         }

         int valuesReaderIndex = buf.readerIndex();
         PacketWrapperImpl wrapper = new PacketWrapperImpl(id, buf, this);

         try {
            this.protocolInfo.getPipeline().transform(direction, this.protocolInfo.getState(direction), wrapper);
         } catch (CancelException var8) {
            throw (CodecException)cancelSupplier.apply(var8);
         }

         this.writeToBuffer(wrapper, buf, id, valuesReaderIndex);
      }
   }

   private void writeToBuffer(PacketWrapperImpl wrapper, ByteBuf buf, int originalId, int originalReaderIndex) {
      int remainingBytes = buf.readableBytes();
      if (buf.readerIndex() == originalReaderIndex && wrapper.areStoredPacketValuesEmpty()) {
         if (wrapper.getId() == originalId) {
            buf.setIndex(0, originalReaderIndex + remainingBytes);
            return;
         }

         if (VarIntType.varIntLength(wrapper.getId()) == VarIntType.varIntLength(originalId)) {
            buf.setIndex(0, 0);
            Types.VAR_INT.writePrimitive(buf, wrapper.getId());
            buf.writerIndex(originalReaderIndex + remainingBytes);
            return;
         }
      }

      ByteBuf remainingBuf = buf.alloc().buffer(remainingBytes, remainingBytes);

      try {
         remainingBuf.writeBytes(buf, remainingBytes);
         buf.setIndex(0, 0);
         wrapper.writeProcessedValues(buf);
         buf.writeBytes(remainingBuf);
      } finally {
         remainingBuf.release();
      }
   }

   @Override
   public long getId() {
      return this.id;
   }

   @Nullable
   @Override
   public Channel getChannel() {
      return this.channel;
   }

   @Override
   public ProtocolInfo getProtocolInfo() {
      return this.protocolInfo;
   }

   @Override
   public Map<Class<?>, StorableObject> getStoredObjects() {
      return this.storedObjects;
   }

   @Override
   public boolean isActive() {
      return this.active;
   }

   @Override
   public void setActive(boolean active) {
      this.active = active;
   }

   @Override
   public boolean isPendingDisconnect() {
      return this.pendingDisconnect;
   }

   @Override
   public void setPendingDisconnect(boolean pendingDisconnect) {
      this.pendingDisconnect = pendingDisconnect;
   }

   @Override
   public boolean isClientSide() {
      return this.clientSide;
   }

   @Override
   public long generatePassthroughToken() {
      while (true) {
         synchronized (this.passthroughTokens) {
            long token = this.random.nextLong();
            if (this.passthroughTokens.add(token)) {
               return token;
            }
         }
      }
   }

   private boolean removePassthroughToken(long token) {
      synchronized (this.passthroughTokens) {
         return this.passthroughTokens.remove(token);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         UserConnectionImpl that = (UserConnectionImpl)o;
         return this.id == that.id;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Long.hashCode(this.id);
   }
}
