package ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.ConnectionState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.UserProfile;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.LogManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.SynchronizedRegistriesHandler;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerConfigurationEnd;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerRegistryData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerLoginSuccess;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class InternalPacketListener extends PacketListenerAbstract {
   private final boolean preVia;

   public InternalPacketListener() {
      this(PacketListenerPriority.LOWEST, false);
   }

   public InternalPacketListener(PacketListenerPriority priority) {
      this(priority, false);
   }

   public InternalPacketListener(PacketListenerPriority priority, boolean preVia) {
      super(priority);
      this.preVia = preVia;
   }

   @Override
   public boolean isPreVia() {
      return this.preVia;
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      User user = event.getUser();
      if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
         if (!this.preVia) {
            Object channel = event.getChannel();
            WrapperLoginServerLoginSuccess loginSuccess = new WrapperLoginServerLoginSuccess(event);
            UserProfile profile = loginSuccess.getUserProfile();
            user.getProfile().setUUID(profile.getUUID());
            user.getProfile().setName(profile.getName());
            user.getProfile().setTextureProperties(profile.getTextureProperties());
            synchronized (channel) {
               PacketEvents.getAPI().getProtocolManager().setChannel(profile.getUUID(), channel);
            }

            if (PacketEvents.getAPI().getLogManager().isDebug()) {
               PacketEvents.getAPI().getLogManager().debug("Mapped player UUID with their channel " + profile.getUUID() + " " + channel);
            }
         }

         if (event.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            if (this.preVia) {
               user.setPreViaEncoderState(ConnectionState.CONFIGURATION);
            } else {
               user.setPostViaEncoderState(ConnectionState.CONFIGURATION);
            }
         } else if (this.preVia) {
            user.setPreViaEncoderState(ConnectionState.PLAY);
            user.setPreViaDecoderState(ConnectionState.PLAY);
         } else {
            user.setPostViaEncoderState(ConnectionState.PLAY);
            user.setPostViaDecoderState(ConnectionState.PLAY);
         }
      }

      if (this.preVia) {
         if (event.getPacketType() == PacketType.Play.Server.CONFIGURATION_START) {
            user.setPreViaEncoderState(ConnectionState.CONFIGURATION);
         } else if (event.getPacketType() == PacketType.Configuration.Server.CONFIGURATION_END) {
            user.setPreViaEncoderState(ConnectionState.PLAY);
         }
      } else {
         if (event.getPacketType() == PacketType.Configuration.Server.REGISTRY_DATA) {
            WrapperConfigServerRegistryData packet = new WrapperConfigServerRegistryData(event);
            if (packet.getElements() != null) {
               SynchronizedRegistriesHandler.handleRegistry(user, packet, packet.getRegistryKey(), packet.getElements());
            }

            if (packet.getRegistryData() != null) {
               SynchronizedRegistriesHandler.handleLegacyRegistries(user, packet, packet.getRegistryData());
            }
         } else if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            WrapperPlayServerJoinGame joinGame = new WrapperPlayServerJoinGame(event);
            user.setEntityId(joinGame.getEntityId());
            if (joinGame.getDimensionCodec() != null) {
               SynchronizedRegistriesHandler.handleLegacyRegistries(user, joinGame, joinGame.getDimensionCodec());
               user.finalizeRegistries(joinGame);
            }

            user.setDimensionType(joinGame.getDimensionType());
         } else if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            WrapperPlayServerRespawn packetx = new WrapperPlayServerRespawn(event);
            user.setDimensionType(packetx.getDimensionType());
         } else if (event.getPacketType() == PacketType.Play.Server.CONFIGURATION_START) {
            user.setPostViaEncoderState(ConnectionState.CONFIGURATION);
         } else if (event.getPacketType() == PacketType.Configuration.Server.CONFIGURATION_END) {
            user.setPostViaEncoderState(ConnectionState.PLAY);
            user.finalizeRegistries(new WrapperConfigServerConfigurationEnd(event));
         }
      }
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      User user = event.getUser();
      if (event.getPacketType() == PacketType.Handshaking.Client.HANDSHAKE) {
         if (this.preVia) {
            return;
         }

         WrapperHandshakingClientHandshake packet = new WrapperHandshakingClientHandshake(event);
         ClientVersion clientVersion = packet.getClientVersion();
         ConnectionState state = packet.getNextConnectionState();
         LogManager logger = PacketEvents.getAPI().getLogManager();
         if (logger.isDebug()) {
            logger.debug("Processed handshake for " + event.getAddress() + ": " + state.name() + " / " + packet.getClientVersion().getReleaseName());
         }

         user.setClientVersion(clientVersion);
         user.setConnectionState(state);
      } else if (event.getPacketType() == PacketType.Login.Client.LOGIN_SUCCESS_ACK) {
         if (this.preVia) {
            user.setPreViaDecoderState(ConnectionState.CONFIGURATION);
         } else {
            user.setPostViaDecoderState(ConnectionState.CONFIGURATION);
         }
      } else if (event.getPacketType() == PacketType.Play.Client.CONFIGURATION_ACK) {
         if (this.preVia) {
            user.setPreViaDecoderState(ConnectionState.CONFIGURATION);
         } else {
            user.setPostViaDecoderState(ConnectionState.CONFIGURATION);
         }
      } else if (event.getPacketType() == PacketType.Configuration.Client.CONFIGURATION_END_ACK) {
         if (this.preVia) {
            user.setPreViaDecoderState(ConnectionState.PLAY);
         } else {
            user.setPostViaDecoderState(ConnectionState.PLAY);
         }
      }
   }
}
