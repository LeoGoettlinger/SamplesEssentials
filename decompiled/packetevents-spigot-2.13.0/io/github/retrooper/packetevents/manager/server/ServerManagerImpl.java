package io.github.retrooper.packetevents.manager.server;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.LogManager;
import com.github.retrooper.packetevents.util.mappings.GlobalRegistryHolder;
import org.bukkit.Bukkit;

public class ServerManagerImpl implements ServerManager {
   private ServerVersion serverVersion;

   private ServerVersion resolveVersionNoCache() {
      LogManager logger = PacketEvents.getAPI().getLogManager();
      String bukkitVersion = Bukkit.getBukkitVersion();
      ServerVersion fallbackVersion = ServerVersion.V_1_8_8;
      String failureToDetectVersionMsg = "Your server software is preventing us from checking the Minecraft Server version. This is what we found: "
         + Bukkit.getBukkitVersion()
         + ". We will assume the Server version is "
         + fallbackVersion.name()
         + "...\n If you need assistance, join our Discord server: https://discord.gg/DVHxPPxHZc";
      if (bukkitVersion.contains("Unknown")) {
         logger.warn(failureToDetectVersionMsg);
         return fallbackVersion;
      } else {
         for (ServerVersion val : ServerVersion.reversedValues()) {
            if (bukkitVersion.contains(val.getReleaseName())) {
               return val;
            }
         }

         logger.warn(failureToDetectVersionMsg);
         return fallbackVersion;
      }
   }

   @Override
   public ServerVersion getVersion() {
      if (this.serverVersion == null) {
         this.serverVersion = this.resolveVersionNoCache();
      }

      return this.serverVersion;
   }

   @Override
   public Object getRegistryCacheKey(User user, ClientVersion version) {
      return GlobalRegistryHolder.getGlobalRegistryCacheKey(user, version);
   }
}
