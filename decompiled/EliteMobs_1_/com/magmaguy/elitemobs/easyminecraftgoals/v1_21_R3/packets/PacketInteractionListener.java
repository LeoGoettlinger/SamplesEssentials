package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R3.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.DamageIndicatorClamp;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInteractionManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketInteractionContext;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketPassengerRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.PlayerConnectionUtils;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayOutWorldParticles;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity.c;
import net.minecraft.server.CancelledPacketHandleException;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.EnumHand;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public class PacketInteractionListener implements Listener {
   private final Plugin plugin;
   private final String handlerName;
   private final Map<UUID, Channel> playerChannels = new ConcurrentHashMap<>();
   private static Field entityIdField;
   private static Field actionField;
   private static Field connectionField;
   private static Field channelField;
   private static Field particleField;
   private static Field countField;

   public PacketInteractionListener(Plugin plugin) {
      this.plugin = plugin;
      this.handlerName = "emg_packet_interaction_" + plugin.getName();
   }

   public void initialize() {
      Bukkit.getPluginManager().registerEvents(this, this.plugin);

      for (Player player : Bukkit.getOnlinePlayers()) {
         this.injectPlayer(player);
      }
   }

   public void shutdown() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         this.uninjectPlayer(player);
      }

      this.playerChannels.clear();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
         if (event.getPlayer().isOnline()) {
            this.injectPlayer(event.getPlayer());
         }
      }, 1L);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.uninjectPlayer(event.getPlayer());
   }

   private void injectPlayer(Player player) {
      try {
         EntityPlayer serverPlayer = ((CraftPlayer)player).getHandle();
         PlayerConnection packetListener = serverPlayer.f;
         Channel channel = this.getChannel(packetListener);
         if (channel == null) {
            return;
         }

         if (channel.pipeline().get(this.handlerName) != null) {
            channel.pipeline().remove(this.handlerName);
         }

         channel.pipeline().addBefore("packet_handler", this.handlerName, new PacketInteractionListener.PacketHandler(player, serverPlayer));
         this.playerChannels.put(player.getUniqueId(), channel);
      } catch (Exception var5) {
         this.plugin.getLogger().warning("Failed to inject packet handler for player " + player.getName() + ": " + var5.getMessage());
      }
   }

   private Channel getChannel(PlayerConnection packetListener) {
      try {
         if (connectionField != null && channelField != null) {
            NetworkManager connection = (NetworkManager)connectionField.get(packetListener);
            return (Channel)channelField.get(connection);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return null;
   }

   private void uninjectPlayer(Player player) {
      PacketPassengerRegistry.clearViewer(player.getUniqueId());
      Channel channel = this.playerChannels.remove(player.getUniqueId());
      if (channel != null && channel.pipeline().get(this.handlerName) != null) {
         try {
            channel.pipeline().remove(this.handlerName);
         } catch (Exception var4) {
         }
      }
   }

   private static PacketPlayOutWorldParticles maybeClampDamageIndicator(PacketPlayOutWorldParticles packet) {
      int cap = DamageIndicatorClamp.getMaxParticles();
      if (cap <= 0) {
         return packet;
      } else if (countField != null && particleField != null) {
         try {
            ParticleParam particle = (ParticleParam)particleField.get(packet);
            if (particle != null && particle.a() == Particles.g) {
               int count = countField.getInt(packet);
               return count <= cap
                  ? packet
                  : new PacketPlayOutWorldParticles(
                     particle, packet.b(), packet.e(), packet.f(), packet.g(), packet.h(), packet.i(), packet.j(), packet.k(), packet.l(), cap
                  );
            } else {
               return packet;
            }
         } catch (Throwable var4) {
            return packet;
         }
      } else {
         return packet;
      }
   }

   private static int getEntityId(PacketPlayInUseEntity packet) {
      try {
         if (entityIdField != null) {
            return entityIdField.getInt(packet);
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return -1;
   }

   private static PacketInteractionContext getInteractionContext(PacketPlayInUseEntity packet) {
      final PacketInteractionContext[] context = new PacketInteractionContext[]{null};
      packet.a(new c() {
         public void a() {
            context[0] = PacketInteractionContext.attack();
         }

         public void a(EnumHand hand) {
            context[0] = PacketInteractionContext.interact(PacketInteractionListener.toEquipmentSlot(hand));
         }

         public void a(EnumHand hand, Vec3D interactionLocation) {
            context[0] = PacketInteractionContext.interactAt(PacketInteractionListener.toEquipmentSlot(hand));
         }
      });
      if (context[0] == null) {
         throw new IllegalStateException("Interaction packet did not dispatch an action");
      } else {
         return context[0];
      }
   }

   private static EquipmentSlot toEquipmentSlot(EnumHand hand) {
      return hand == EnumHand.a ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
   }

   static {
      try {
         entityIdField = PacketPlayInUseEntity.class.getDeclaredField("entityId");
         entityIdField.setAccessible(true);
      } catch (NoSuchFieldException var9) {
         try {
            entityIdField = PacketPlayInUseEntity.class.getDeclaredField("a");
            entityIdField.setAccessible(true);
         } catch (NoSuchFieldException var8) {
            var8.printStackTrace();
         }
      }

      try {
         actionField = PacketPlayInUseEntity.class.getDeclaredField("action");
         actionField.setAccessible(true);
      } catch (NoSuchFieldException var7) {
         try {
            actionField = PacketPlayInUseEntity.class.getDeclaredField("b");
            actionField.setAccessible(true);
         } catch (NoSuchFieldException var6) {
            var6.printStackTrace();
         }
      }

      for (Field f : ServerCommonPacketListenerImpl.class.getDeclaredFields()) {
         if (NetworkManager.class.isAssignableFrom(f.getType())) {
            connectionField = f;
            connectionField.setAccessible(true);
            break;
         }
      }

      for (Field fx : NetworkManager.class.getDeclaredFields()) {
         if (Channel.class.isAssignableFrom(fx.getType())) {
            channelField = fx;
            channelField.setAccessible(true);
            break;
         }
      }

      try {
         Class<?> cls = PacketPlayOutWorldParticles.class;
         List<Field> intFields = new ArrayList<>();

         for (Field fxx : cls.getDeclaredFields()) {
            if (!Modifier.isStatic(fxx.getModifiers())) {
               if (fxx.getType() == int.class) {
                  intFields.add(fxx);
               } else if (particleField == null && ParticleParam.class.isAssignableFrom(fxx.getType())) {
                  fxx.setAccessible(true);
                  particleField = fxx;
               }
            }
         }

         for (Field fxxx : intFields) {
            if (fxxx.getName().equals("count")) {
               countField = fxxx;
               break;
            }
         }

         if (countField == null && intFields.size() == 1) {
            countField = intFields.get(0);
         }

         if (countField != null) {
            countField.setAccessible(true);
         }

         if (countField == null && intFields.size() > 1) {
            StringBuilder names = new StringBuilder();

            for (Field fxxxx : intFields) {
               names.append(fxxxx.getName()).append(' ');
            }

            Bukkit.getLogger()
               .warning(
                  "[MagmaCore] Damage-indicator clamp: could not identify the particle count field on "
                     + cls.getName()
                     + " (int fields: "
                     + names.toString().trim()
                     + "). damageIndicatorParticleCap will have no effect on this server version — please report this."
               );
         } else if (countField == null || particleField == null) {
            Bukkit.getLogger()
               .warning(
                  "[MagmaCore] Damage-indicator clamp disabled on this server version (count/particle field unresolved); damageIndicatorParticleCap will have no effect."
               );
         }
      } catch (Throwable var10) {
         particleField = null;
         countField = null;
         Bukkit.getLogger().warning("[MagmaCore] Damage-indicator clamp failed to initialize: " + var10 + "; damageIndicatorParticleCap will have no effect.");
      }
   }

   private class PacketHandler extends ChannelDuplexHandler {
      private final Player player;
      private final EntityPlayer serverPlayer;

      public PacketHandler(Player player, EntityPlayer serverPlayer) {
         this.player = player;
         this.serverPlayer = serverPlayer;
      }

      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
         if (msg instanceof PacketPlayInUseEntity packet) {
            try {
               int entityId = PacketInteractionListener.getEntityId(packet);
               PacketInteractionContext interactionContext = PacketInteractionListener.getInteractionContext(packet);
               PacketEntityInteractionManager interactionManager = PacketEntityInteractionManager.getInstance();
               PacketEntityInteractionManager.Dispatch dispatch = interactionManager.captureDispatch(entityId);
               if (dispatch != null) {
                  PacketEntityInteractionManager.PreparedInteraction prepared = dispatch.prepare(interactionContext);
                  switch (prepared.decision()) {
                     case ROUTE:
                        this.routeInPacketOrder(packet.a(), prepared);
                        return;
                     case CONSUME:
                        return;
                     case PASS:
                  }
               }
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         }

         super.channelRead(ctx, msg);
      }

      private void routeInPacketOrder(
         final PacketType<? extends Packet<PacketListenerPlayIn>> packetType, final PacketEntityInteractionManager.PreparedInteraction prepared
      ) {
         Packet<PacketListenerPlayIn> routed = new Packet<PacketListenerPlayIn>() {
            public PacketType<? extends Packet<PacketListenerPlayIn>> a() {
               return packetType;
            }

            public void handle(PacketListenerPlayIn listener) {
               PlayerConnectionUtils.a(this, listener, (WorldServer)PacketHandler.this.serverPlayer.dV());
               if (PacketInteractionListener.this.plugin.isEnabled() && PacketHandler.this.player.isOnline()) {
                  try {
                     prepared.route(PacketHandler.this.player);
                  } catch (Throwable var3) {
                     PacketInteractionListener.this.plugin
                        .getLogger()
                        .log(Level.SEVERE, "Failed to route packet entity interaction for " + PacketHandler.this.player.getName(), var3);
                  }
               }
            }
         };

         try {
            routed.a(this.serverPlayer.f);
         } catch (CancelledPacketHandleException var5) {
         }
      }

      public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
         if (msg instanceof PacketPlayOutWorldParticles particlesPacket) {
            msg = PacketInteractionListener.maybeClampDamageIndicator(particlesPacket);
         }

         if (msg instanceof Packet<?> packet) {
            msg = PassengerPackets.compose(this.player.getUniqueId(), packet);
         }

         super.write(ctx, msg, promise);
      }
   }
}
