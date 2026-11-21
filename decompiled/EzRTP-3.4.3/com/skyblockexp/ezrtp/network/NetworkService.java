package com.skyblockexp.ezrtp.network;

import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class NetworkService {
   private final JavaPlugin plugin;
   private final NetworkConfiguration configuration;
   private final Logger logger;
   private final Map<String, NetworkConfiguration.ServerStatusSnapshot> statusByServer = new ConcurrentHashMap<>();
   private BukkitTask pingTask;

   public NetworkService(JavaPlugin plugin, NetworkConfiguration configuration, Logger logger) {
      this.plugin = plugin;
      this.configuration = configuration;
      this.logger = logger;
      this.initialise();
   }

   private void initialise() {
      if (this.configuration != null && !this.configuration.getServers().isEmpty()) {
         for (NetworkConfiguration.NetworkServer server : this.configuration.getServers()) {
            this.statusByServer.put(server.getId(), NetworkConfiguration.ServerStatusSnapshot.unknown());
         }

         try {
            this.pingTask = this.plugin
               .getServer()
               .getScheduler()
               .runTaskTimerAsynchronously(this.plugin, this::refreshStatuses, 1L, this.configuration.getPingIntervalTicks());
         } catch (IllegalStateException var3) {
            this.logger.warning("Unable to start network status polling: " + var3.getMessage());
         }
      }
   }

   private void refreshStatuses() {
      for (NetworkConfiguration.NetworkServer server : this.configuration.getServers()) {
         NetworkConfiguration.ServerStatusSnapshot status = this.pingServer(server);
         this.statusByServer.put(server.getId(), status);
      }
   }

   private NetworkConfiguration.ServerStatusSnapshot pingServer(NetworkConfiguration.NetworkServer server) {
      long start = System.nanoTime();

      try {
         NetworkConfiguration.ServerStatusSnapshot var7;
         try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(server.getHost(), server.getPort()), this.configuration.getPingTimeoutMillis());
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            var7 = NetworkConfiguration.ServerStatusSnapshot.online(elapsed);
         }

         return var7;
      } catch (IOException var10) {
         return NetworkConfiguration.ServerStatusSnapshot.offline();
      }
   }

   public NetworkConfiguration.ServerStatusSnapshot getStatus(NetworkConfiguration.NetworkServer server) {
      return server == null
         ? NetworkConfiguration.ServerStatusSnapshot.unknown()
         : this.statusByServer.getOrDefault(server.getId(), NetworkConfiguration.ServerStatusSnapshot.unknown());
   }

   public ItemStack createIcon(NetworkConfiguration.NetworkServer server) {
      NetworkConfiguration.ServerStatusSnapshot status = this.getStatus(server);
      return server.getIconTemplate().createIcon(status, server.getDisplayName());
   }

   public void transferPlayer(Player player, NetworkConfiguration.NetworkServer server) {
      if (player != null && server != null) {
         if (server.allowWhenOffline() || !this.getStatus(server).isOffline()) {
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

            try (DataOutputStream output = new DataOutputStream(outputBuffer)) {
               output.writeUTF("Connect");
               output.writeUTF(server.getBungeeServer());
            } catch (IOException var9) {
               this.logger.warning(String.format("Failed to prepare network transfer to '%s': %s", server.getBungeeServer(), var9.getMessage()));
               return;
            }

            player.sendPluginMessage(this.plugin, "BungeeCord", outputBuffer.toByteArray());
            Component message = server.connectMessage();
            if (message != null && !Objects.equals(message, Component.empty())) {
               MessageUtil.send(player, message);
            }
         }
      }
   }

   public void shutdown() {
      if (this.pingTask != null) {
         try {
            this.pingTask.cancel();
         } catch (Exception var2) {
         }

         this.pingTask = null;
      }

      this.statusByServer.clear();
   }
}
