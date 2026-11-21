package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.ConnectionState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.ChatType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.ResolvableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTList;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.Dimension;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.dimension.DimensionType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.dimension.DimensionTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.IRegistry;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.IRegistryHolder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleSubtitle;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleText;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleTimes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTitle;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class User implements IRegistryHolder {
   private final Object channel;
   private ConnectionState decoderState;
   private ConnectionState encoderState;
   private ConnectionState preViaDecoderState;
   private ConnectionState preViaEncoderState;
   private ClientVersion clientVersion;
   private final UserProfile profile;
   private int entityId = -1;
   private DimensionType dimensionType = DimensionTypes.OVERWORLD;
   private final Map<ResourceLocation, IRegistry<?>> registries = new ConcurrentHashMap<>();

   public User(Object channel, ConnectionState connectionState, ClientVersion clientVersion, UserProfile profile) {
      this.channel = channel;
      this.decoderState = connectionState;
      this.encoderState = connectionState;
      this.preViaDecoderState = connectionState;
      this.preViaEncoderState = connectionState;
      this.clientVersion = clientVersion;
      this.profile = profile;
   }

   @ApiStatus.Internal
   @Nullable
   @Override
   public IRegistry<?> getRegistry(ResourceLocation registryKey, ClientVersion version) {
      return this.registries.get(registryKey);
   }

   @ApiStatus.Internal
   public void putRegistry(IRegistry<?> registry) {
      this.registries.put(registry.getRegistryKey(), registry);
   }

   @ApiStatus.Internal
   public void finalizeRegistries(PacketWrapper<?> wrapper) {
      for (IRegistry<?> registry : this.registries.values()) {
         for (MappedEntity entry : registry.getEntries()) {
            if (entry instanceof ResolvableEntity) {
               ((ResolvableEntity)entry).doResolve(wrapper);
            }
         }
      }
   }

   public Object getChannel() {
      return this.channel;
   }

   public InetSocketAddress getAddress() {
      return (InetSocketAddress)ChannelHelper.remoteAddress(this.channel);
   }

   @ApiStatus.Obsolete
   public ConnectionState getConnectionState() throws IllegalStateException {
      ConnectionState decoderState = this.decoderState;
      ConnectionState encoderState = this.encoderState;
      if (decoderState != encoderState) {
         throw new IllegalStateException("Can't get common connection state: " + decoderState + " != " + encoderState);
      } else {
         return decoderState;
      }
   }

   @ApiStatus.Internal
   public void setConnectionState(ConnectionState connectionState) {
      this.setDecoderState(connectionState);
      this.setEncoderState(connectionState);
   }

   public ConnectionState getDecoderState() {
      return this.decoderState;
   }

   @ApiStatus.Internal
   public void setDecoderState(ConnectionState decoderState) {
      this.setPreViaDecoderState(decoderState);
      this.setPostViaDecoderState(decoderState);
   }

   public ConnectionState getEncoderState() {
      return this.encoderState;
   }

   @ApiStatus.Internal
   public void setEncoderState(ConnectionState encoderState) {
      this.setPreViaEncoderState(encoderState);
      this.setPostViaEncoderState(encoderState);
   }

   public ConnectionState getPostViaDecoderState() {
      return this.decoderState;
   }

   public ConnectionState getPostViaEncoderState() {
      return this.encoderState;
   }

   public ConnectionState getPreViaDecoderState() {
      return this.preViaDecoderState;
   }

   public ConnectionState getPreViaEncoderState() {
      return this.preViaEncoderState;
   }

   @ApiStatus.Internal
   public void setPostViaDecoderState(ConnectionState connectionState) {
      this.decoderState = connectionState;
      PacketEvents.getAPI().getLogManager().debug("Transitioned " + this.getName() + "'s post-Via decoder into " + connectionState + " state!");
   }

   @ApiStatus.Internal
   public void setPostViaEncoderState(ConnectionState connectionState) {
      this.encoderState = connectionState;
      PacketEvents.getAPI().getLogManager().debug("Transitioned " + this.getName() + "'s post-Via encoder into " + connectionState + " state!");
   }

   @ApiStatus.Internal
   public void setPreViaDecoderState(ConnectionState connectionState) {
      this.preViaDecoderState = connectionState;
      PacketEvents.getAPI().getLogManager().debug("Transitioned " + this.getName() + "'s pre-Via decoder into " + connectionState + " state!");
   }

   @ApiStatus.Internal
   public void setPreViaEncoderState(ConnectionState connectionState) {
      this.preViaEncoderState = connectionState;
      PacketEvents.getAPI().getLogManager().debug("Transitioned " + this.getName() + "'s pre-Via encoder into " + connectionState + " state!");
   }

   public ClientVersion getClientVersion() {
      return this.clientVersion;
   }

   public void setClientVersion(ClientVersion clientVersion) {
      this.clientVersion = clientVersion;
   }

   public UserProfile getProfile() {
      return this.profile;
   }

   public String getName() {
      return this.profile.getName();
   }

   public UUID getUUID() {
      return this.profile.getUUID();
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public void sendPacket(Object buffer) {
      PacketEvents.getAPI().getProtocolManager().sendPacket(this.channel, buffer);
   }

   public void sendPacket(PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().sendPacket(this.channel, wrapper);
   }

   public void sendPacketSilently(Object buffer) {
      PacketEvents.getAPI().getProtocolManager().sendPacketSilently(this.channel, buffer);
   }

   public void sendPacketSilently(PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().sendPacketSilently(this.channel, wrapper);
   }

   public void writePacket(Object buffer) {
      PacketEvents.getAPI().getProtocolManager().writePacket(this.channel, buffer);
   }

   public void writePacket(PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().writePacket(this.channel, wrapper);
   }

   public void writePacketSilently(Object buffer) {
      PacketEvents.getAPI().getProtocolManager().writePacketSilently(this.channel, buffer);
   }

   public void writePacketSilently(PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().writePacketSilently(this.channel, wrapper);
   }

   public void receivePacket(Object buffer) {
      PacketEvents.getAPI().getProtocolManager().receivePacket(this.channel, buffer);
   }

   public void receivePacket(PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().receivePacket(this.channel, wrapper);
   }

   public void receivePacketSilently(Object buffer) {
      PacketEvents.getAPI().getProtocolManager().receivePacketSilently(this.channel, buffer);
   }

   public void receivePacketSilently(PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().receivePacketSilently(this.channel, wrapper);
   }

   public void flushPackets() {
      ChannelHelper.flush(this.channel);
   }

   public void closeConnection() {
      ChannelHelper.close(this.channel);
   }

   public void closeInventory() {
      WrapperPlayServerCloseWindow closeWindow = new WrapperPlayServerCloseWindow(0);
      PacketEvents.getAPI().getProtocolManager().sendPacket(this.channel, (PacketWrapper<?>)closeWindow);
   }

   public void sendMessage(String legacyMessage) {
      this.sendMessage(this.getSerializers().fromLegacy(legacyMessage));
   }

   public void sendMessage(Component component) {
      this.sendMessage(component, ChatTypes.CHAT);
   }

   public void sendMessage(Component component, ChatType type) {
      ClientVersion version = this.getPacketVersion();
      PacketWrapper<?> chatPacket;
      if (version.isNewerThanOrEquals(ClientVersion.V_1_19)) {
         chatPacket = new WrapperPlayServerSystemChatMessage(false, component);
      } else {
         ChatMessage message;
         if (version.isNewerThanOrEquals(ClientVersion.V_1_16)) {
            message = new ChatMessage_v1_16(component, type, new UUID(0L, 0L));
         } else {
            message = new ChatMessageLegacy(component, type);
         }

         chatPacket = new WrapperPlayServerChatMessage(message);
      }

      PacketEvents.getAPI().getProtocolManager().sendPacket(this.channel, chatPacket);
   }

   public void sendTitle(String legacyTitle, String legacySubtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
      LegacyComponentSerializer serializer = this.getSerializers().legacy();
      Component title = serializer.deserialize(legacyTitle);
      Component subtitle = serializer.deserialize(legacySubtitle);
      this.sendTitle(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
   }

   public void sendTitle(Component title, Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
      boolean modern = this.getPacketVersion().isNewerThanOrEquals(ClientVersion.V_1_17);
      PacketWrapper<?> setTitle = null;
      PacketWrapper<?> setSubtitle = null;
      PacketWrapper<?> animation;
      if (modern) {
         animation = new WrapperPlayServerSetTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);
         if (title != null) {
            setTitle = new WrapperPlayServerSetTitleText(title);
         }

         if (subtitle != null) {
            setSubtitle = new WrapperPlayServerSetTitleSubtitle(subtitle);
         }
      } else {
         animation = new WrapperPlayServerTitle(
            WrapperPlayServerTitle.TitleAction.SET_TIMES_AND_DISPLAY, (Component)null, null, null, fadeInTicks, stayTicks, fadeOutTicks
         );
         if (title != null) {
            setTitle = new WrapperPlayServerTitle(WrapperPlayServerTitle.TitleAction.SET_TITLE, title, null, null, 0, 0, 0);
         }

         if (subtitle != null) {
            setSubtitle = new WrapperPlayServerTitle(WrapperPlayServerTitle.TitleAction.SET_SUBTITLE, null, subtitle, null, 0, 0, 0);
         }
      }

      this.sendPacket(animation);
      if (setTitle != null) {
         this.sendPacket(setTitle);
      }

      if (setSubtitle != null) {
         this.sendPacket(setSubtitle);
      }
   }

   public ClientVersion getPacketVersion() {
      PacketEventsAPI<?> api = PacketEvents.getAPI();
      return api.getInjector().isProxy() ? this.getClientVersion() : api.getServerManager().getVersion().toClientVersion();
   }

   public AdventureSerializer getSerializers() {
      return AdventureSerializer.serializer(this.getPacketVersion());
   }

   public int getMinWorldHeight() {
      return this.dimensionType.getMinY();
   }

   @Deprecated
   public int getMinWorldHeight(@Nullable ClientVersion version) {
      return this.getMinWorldHeight();
   }

   public int getTotalWorldHeight() {
      return this.dimensionType.getHeight();
   }

   @Deprecated
   public int getTotalWorldHeight(@Nullable ClientVersion version) {
      return this.getTotalWorldHeight();
   }

   public DimensionType getDimensionType() {
      return this.dimensionType;
   }

   @ApiStatus.Internal
   public void setDimensionType(DimensionType dimensionType) {
      this.dimensionType = dimensionType;
   }

   @Deprecated
   public void setMinWorldHeight(int minWorldHeight) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public void setTotalWorldHeight(int totalWorldHeight) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public void switchDimensionType(ServerVersion version, Dimension dimension) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public void setDefaultWorldHeights(ServerVersion version, Dimension dimension) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public void setDefaultWorldHeights(boolean extended) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public void setWorldNBT(NBTList<NBTCompound> worldNBT) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public Dimension getDimension() {
      return Dimension.fromDimensionType(this.dimensionType, this, null);
   }

   @Deprecated
   public void setDimension(Dimension dimension) {
      this.dimensionType = dimension.asDimensionType(this, null);
   }

   @Deprecated
   @Nullable
   public NBTCompound getWorldNBT(String worldName) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Nullable
   public NBTCompound getWorldNBT(int worldId) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Nullable
   public NBTCompound getWorldNBT(Dimension dimension) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Nullable
   public String getWorldName(int worldId) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public String getWorldName(Dimension dimension) {
      throw new UnsupportedOperationException();
   }
}
