package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.wrapper;

import com.artillexstudios.axtrade.libs.axapi.items.HashGenerator;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.packet.ChannelDuplexHandlerPacketListener;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.packet.PacketTransformer;
import com.artillexstudios.axtrade.libs.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axtrade.libs.axapi.reflection.FieldAccessor;
import com.artillexstudios.axtrade.libs.axapi.utils.ComponentSerializer;
import com.artillexstudios.axtrade.libs.axapi.utils.PlayerTextures;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeMapBase;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.inventory.ContainerSynchronizer;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_21_R4.attribute.CraftAttribute;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class ServerPlayerWrapper implements com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerPlayerWrapper {
   private static final FieldAccessor connectionAccessor = FieldAccessor.builder()
      .withClass("net.minecraft.server.network.ServerCommonPacketListenerImpl")
      .withField("e")
      .build();
   private static final FieldAccessor channelAccessor = FieldAccessor.builder().withClass("net.minecraft.network.NetworkManager").withField("n").build();
   private static final FieldAccessor attributeAccessor = FieldAccessor.builder().withClass(AttributeMapBase.class).withField("d").build();
   private static final FieldAccessor containerSynchronizerAccessor = FieldAccessor.builder().withClass(EntityPlayer.class).withField("dm").build();
   private static final FieldAccessor cacheAccessor = FieldAccessor.builder().withClass("net.minecraft.server.level.EntityPlayer$1").withField("b").build();
   private final PacketTransformer transformer;
   private LoadingCache<Object, Integer> cache;
   private Player wrapped;
   private EntityPlayer serverPlayer;

   public ServerPlayerWrapper(Player player) {
      this.wrapped = player;
      this.transformer = new PacketTransformer(this);
   }

   public ServerPlayerWrapper(EntityPlayer player) {
      this.serverPlayer = player;
      this.transformer = new PacketTransformer(this);
   }

   @Override
   public void inject() {
      this.update();
      NetworkManager connection = connectionAccessor.get(this.serverPlayer.f, NetworkManager.class);
      Channel channel = channelAccessor.get(connection, Channel.class);
      if (channel.pipeline().names().contains("packet_handler")) {
         if (!channel.pipeline().names().contains(AXAPI_HANDLER)) {
            channel.eventLoop().submit(() -> channel.pipeline().addBefore("packet_handler", AXAPI_HANDLER, new ChannelDuplexHandlerPacketListener(this)));
         }
      }
   }

   @Override
   public void uninject() {
      this.update();
      NetworkManager connection = connectionAccessor.get(this.serverPlayer.f, NetworkManager.class);
      Channel channel = channelAccessor.get(connection, Channel.class);
      channel.eventLoop().submit(() -> {
         if (channel.pipeline().get(AXAPI_HANDLER) != null) {
            channel.pipeline().remove(AXAPI_HANDLER);
         }
      });
   }

   @Override
   public void sendPacket(Object packet) {
      this.update();
      if (packet instanceof PacketWrapper wrapper) {
         this.serverPlayer.f.b((Packet)wrapper.cached());
      } else if (packet instanceof Packet<?> p) {
         this.serverPlayer.f.b(p);
      } else {
         LogUtils.warn("Failed to send unknown packet to player {}! Packet: {}", this.wrapped().getName(), packet);
      }
   }

   @Override
   public void message(Component message) {
      this.update();
      this.serverPlayer.f.b(new ClientboundSystemChatPacket(ComponentSerializer.INSTANCE.toVanilla(message), false));
   }

   @Override
   public double getBase(Attribute attribute) {
      this.update();
      AttributeMapBase map = this.serverPlayer.fa();
      AttributeProvider supplier = attributeAccessor.get(map, AttributeProvider.class);
      return supplier.b(CraftAttribute.bukkitToMinecraftHolder(attribute));
   }

   @Override
   public PlayerTextures textures() {
      this.update();
      GameProfile profile = this.serverPlayer.gi();
      Optional<Property> property = profile.getProperties().get("textures").stream().findFirst();
      if (property.isEmpty()) {
         return new PlayerTextures(null, null);
      } else {
         Property value = property.get();
         return new PlayerTextures(value.value(), value.signature());
      }
   }

   @Override
   public double getX() {
      this.update();
      return this.serverPlayer.dA();
   }

   @Override
   public double getZ() {
      this.update();
      return this.serverPlayer.dG();
   }

   @Override
   public HashGenerator hashGenerator() {
      this.update();
      return typed -> (Integer)this.cache.getUnchecked(typed);
   }

   public Player wrapped() {
      Player wrapped = this.wrapped;
      if (wrapped == null) {
         wrapped = this.serverPlayer.getBukkitEntity();
         this.wrapped = wrapped;
      }

      return wrapped;
   }

   @Override
   public void update(boolean force) {
      if (this.serverPlayer == null || force) {
         this.serverPlayer = ((CraftPlayer)this.wrapped).getHandle();
         ContainerSynchronizer synchronizer = containerSynchronizerAccessor.get(this.serverPlayer, ContainerSynchronizer.class);
         this.cache = cacheAccessor.getUnchecked(synchronizer);
      }
   }

   public EntityPlayer asMinecraft() {
      this.update();
      return this.serverPlayer;
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof ServerPlayerWrapper that) {
         this.update();
         that.update();
         return Objects.equals(this.serverPlayer, that.serverPlayer) ? true : this.wrapped().getUniqueId().equals(that.wrapped().getUniqueId());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      this.update();
      return Objects.hashCode(this.serverPlayer);
   }
}
