package io.github.retrooper.packetevents.manager.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.util.PlayerPingAccessorModern;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.github.retrooper.packetevents.util.protocolsupport.ProtocolSupportUtil;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class PlayerManagerImpl implements PlayerManager {
   @Internal
   public final Map<UUID, WeakReference<Player>> joiningPlayers = new ConcurrentHashMap<>();

   @Override
   public int getPing(@NotNull Object player) {
      return SpigotReflectionUtil.V_1_17_OR_HIGHER
         ? PlayerPingAccessorModern.getPing((Player)player)
         : SpigotReflectionUtil.getPlayerPingLegacy((Player)player);
   }

   @NotNull
   @Override
   public ClientVersion getClientVersion(@NotNull Object p) {
      Player player = (Player)p;
      User user = this.getUser(player);
      if (user == null) {
         return ClientVersion.UNKNOWN;
      } else {
         if (user.getClientVersion() == null) {
            int protocolVersion;
            if (ProtocolSupportUtil.isAvailable()) {
               protocolVersion = ProtocolSupportUtil.getProtocolVersion(user.getAddress());
               PacketEvents.getAPI()
                  .getLogManager()
                  .debug("Requested ProtocolSupport for user " + user.getName() + "'s protocol version. Protocol version: " + protocolVersion);
            } else if (ViaVersionUtil.isAvailable()) {
               protocolVersion = ViaVersionUtil.getProtocolVersion(player);
               PacketEvents.getAPI()
                  .getLogManager()
                  .debug("Requested ViaVersion for " + player.getName() + "'s protocol version. Protocol version: " + protocolVersion);
            } else {
               protocolVersion = PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion();
               PacketEvents.getAPI()
                  .getLogManager()
                  .debug(
                     "No protocol translation plugins are available. We will assume "
                        + user.getName()
                        + "'s protocol version is the same as the server's protocol version. Protocol version: "
                        + protocolVersion
                  );
            }

            ClientVersion version = ClientVersion.getById(protocolVersion);
            user.setClientVersion(version);
         }

         return user.getClientVersion();
      }
   }

   @Override
   public Object getChannel(@NotNull Object player) {
      UUID uuid = ((Player)player).getUniqueId();
      ProtocolManager protocolManager = PacketEvents.getAPI().getProtocolManager();
      Object channel = protocolManager.getChannel(uuid);
      if (channel == null) {
         channel = SpigotReflectionUtil.getChannel((Player)player);
         if (channel != null) {
            synchronized (channel) {
               if (ChannelHelper.isOpen(channel)) {
                  protocolManager.setChannel(uuid, channel);
               }
            }
         }
      }

      return channel;
   }

   @Override
   public User getUser(@NotNull Object player) {
      Player p = (Player)player;
      Object channel = this.getChannel(p);
      return channel == null ? null : PacketEvents.getAPI().getProtocolManager().getUser(channel);
   }
}
