package com.skyblockexp.ezrtp.config.network;

import com.skyblockexp.ezrtp.util.ItemFlagUtil;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.ItemMetaCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class NetworkConfiguration {
   private final boolean enabled;
   private final boolean lobbyServer;
   private final long pingIntervalTicks;
   private final int pingTimeoutMillis;
   private final List<NetworkConfiguration.NetworkServer> servers;

   private NetworkConfiguration(
      boolean enabled, boolean lobbyServer, long pingIntervalTicks, int pingTimeoutMillis, List<NetworkConfiguration.NetworkServer> servers
   ) {
      this.enabled = enabled;
      this.lobbyServer = lobbyServer;
      this.pingIntervalTicks = pingIntervalTicks;
      this.pingTimeoutMillis = pingTimeoutMillis;
      this.servers = servers;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isLobbyServer() {
      return this.lobbyServer;
   }

   public long getPingIntervalTicks() {
      return this.pingIntervalTicks;
   }

   public int getPingTimeoutMillis() {
      return this.pingTimeoutMillis;
   }

   public List<NetworkConfiguration.NetworkServer> getServers() {
      return this.servers;
   }

   public static NetworkConfiguration disabled() {
      return new NetworkConfiguration(false, false, 200L, 1500, Collections.emptyList());
   }

   public static NetworkConfiguration fromConfiguration(ConfigurationSection section, Logger logger) {
      if (section == null) {
         return disabled();
      } else {
         boolean enabled = section.getBoolean("enabled", false);
         boolean lobbyServer = section.getBoolean("lobby", false);
         long pingInterval = Math.max(20L, section.getLong("ping-interval-ticks", 200L));
         int pingTimeout = Math.max(250, section.getInt("ping-timeout-millis", 1500));
         List<NetworkConfiguration.NetworkServer> servers = parseServers(section.getConfigurationSection("servers"), logger);
         return new NetworkConfiguration(enabled, lobbyServer, pingInterval, pingTimeout, Collections.unmodifiableList(servers));
      }
   }

   private static List<NetworkConfiguration.NetworkServer> parseServers(ConfigurationSection section, Logger logger) {
      if (section == null) {
         return Collections.emptyList();
      } else {
         List<NetworkConfiguration.NetworkServer> servers = new ArrayList<>();
         int nextSlot = 0;

         for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry != null) {
               String bungeeName = entry.getString("bungee-server", key);
               if (bungeeName != null && !bungeeName.isBlank()) {
                  String host = entry.getString("host", "127.0.0.1");
                  int port = entry.getInt("port", 25565);
                  if (port > 0 && port <= 65535) {
                     int slot;
                     if (entry.contains("slot")) {
                        slot = entry.getInt("slot");
                     } else {
                        slot = nextSlot;
                     }

                     nextSlot = Math.min(53, slot + 1);
                     String permission = entry.getString("permission", "");
                     String displayName = entry.getString("display-name", key);
                     boolean hideWhenOffline = entry.getBoolean("hide-when-offline", false);
                     boolean allowWhenOffline = entry.getBoolean("allow-when-offline", false);
                     String connectMessageRaw = entry.getString("connect-message", "<gray>Connecting you to <white><server></white>...</gray>");
                     Component connectMessage = Component.empty();
                     if (connectMessageRaw != null && !connectMessageRaw.isBlank()) {
                        String replaced = connectMessageRaw.replace("<server>", displayName);
                        connectMessage = MessageUtil.parseMiniMessage(replaced);
                     }

                     String offlineMessageRaw = entry.getString("offline-message", "<red><server></red> is currently unavailable.");
                     Component offlineMessage = Component.empty();
                     if (offlineMessageRaw != null && !offlineMessageRaw.isBlank()) {
                        String replaced = offlineMessageRaw.replace("<server>", displayName);
                        offlineMessage = MessageUtil.parseMiniMessage(replaced);
                     }

                     NetworkConfiguration.IconTemplate iconTemplate = NetworkConfiguration.IconTemplate.fromConfiguration(
                        entry.getConfigurationSection("icon"), displayName
                     );
                     if (iconTemplate == null) {
                        logger.warning(String.format("Network server '%s' has an invalid icon configuration.", key));
                     } else {
                        NetworkConfiguration.NetworkServer server = new NetworkConfiguration.NetworkServer(
                           key,
                           bungeeName,
                           host,
                           port,
                           permission == null ? "" : permission,
                           slot,
                           displayName,
                           hideWhenOffline,
                           allowWhenOffline,
                           connectMessage,
                           offlineMessage,
                           iconTemplate
                        );
                        servers.add(server);
                     }
                  } else {
                     logger.warning(String.format("Network server '%s' specifies an invalid port '%d'.", key, port));
                  }
               } else {
                  logger.warning(String.format("Network server '%s' is missing a valid 'bungee-server' identifier.", key));
               }
            }
         }

         return servers;
      }
   }

   public static final class IconTemplate {
      private final Material material;
      private final int amount;
      private final Integer customModelData;
      private final String displayName;
      private final List<String> loreLines;

      private IconTemplate(Material material, int amount, Integer customModelData, String displayName, List<String> loreLines) {
         this.material = material;
         this.amount = amount;
         this.customModelData = customModelData;
         this.displayName = displayName;
         this.loreLines = loreLines;
      }

      public ItemStack createIcon(NetworkConfiguration.ServerStatusSnapshot status, String displayName) {
         ItemStack itemStack = new ItemStack(this.material, this.amount);
         ItemMeta meta = itemStack.getItemMeta();
         String providedDisplayName = displayName != null ? displayName : "";
         String templateDisplayName = this.displayName != null ? this.displayName : "";
         boolean hasTemplateName = !templateDisplayName.isBlank();
         boolean hasProvidedName = !providedDisplayName.isBlank();
         if (hasTemplateName || hasProvidedName) {
            String name = hasTemplateName ? templateDisplayName : providedDisplayName;
            String resolvedName = name.replace("<server>", providedDisplayName)
               .replace("<ping>", status.pingDisplay())
               .replace("<status>", status.statusMiniMessage());
            Component component = MessageUtil.parseMiniMessage(resolvedName);
            ItemMetaCompat.setDisplayName(meta, component);
         }

         if (!this.loreLines.isEmpty()) {
            List<Component> lore = new ArrayList<>(this.loreLines.size());

            for (String line : this.loreLines) {
               String resolvedLine = line.replace("<server>", providedDisplayName)
                  .replace("<ping>", status.pingDisplay())
                  .replace("<status>", status.statusMiniMessage());
               lore.add(MessageUtil.parseMiniMessage(resolvedLine));
            }

            ItemMetaCompat.setLore(meta, lore);
         }

         if (this.customModelData != null) {
            meta.setCustomModelData(this.customModelData);
         }

         ItemFlagUtil.applyStandardHideFlags(meta);
         ItemFlagUtil.setItemMetaCompatibly(itemStack, meta);
         return itemStack;
      }

      private static NetworkConfiguration.IconTemplate fromConfiguration(ConfigurationSection section, String displayName) {
         if (section == null) {
            return null;
         } else {
            String materialKey = section.getString("material", "ENDER_PEARL");
            Material material = parseMaterial(materialKey, Material.ENDER_PEARL);
            int amount = Math.max(1, section.getInt("amount", 1));
            Integer customModelData = section.contains("custom-model-data") ? section.getInt("custom-model-data") : null;
            String name = section.getString("name", "<green><server></green>");
            List<String> lore = section.isList("lore") ? section.getStringList("lore") : Collections.emptyList();
            return new NetworkConfiguration.IconTemplate(material, amount, customModelData, name, lore);
         }
      }

      private static Material parseMaterial(String key, Material defaultMaterial) {
         if (key != null && !key.isBlank()) {
            Material material = Material.matchMaterial(key.trim().toUpperCase(Locale.ROOT));
            return material != null ? material : defaultMaterial;
         } else {
            return defaultMaterial;
         }
      }
   }

   public static final class NetworkServer {
      private final String id;
      private final String bungeeServer;
      private final String host;
      private final int port;
      private final String permission;
      private final int slot;
      private final String displayName;
      private final boolean hideWhenOffline;
      private final boolean allowWhenOffline;
      private final Component connectMessage;
      private final Component offlineMessage;
      private final NetworkConfiguration.IconTemplate iconTemplate;

      private NetworkServer(
         String id,
         String bungeeServer,
         String host,
         int port,
         String permission,
         int slot,
         String displayName,
         boolean hideWhenOffline,
         boolean allowWhenOffline,
         Component connectMessage,
         Component offlineMessage,
         NetworkConfiguration.IconTemplate iconTemplate
      ) {
         this.id = id;
         this.bungeeServer = bungeeServer;
         this.host = host;
         this.port = port;
         this.permission = permission;
         this.slot = slot;
         this.displayName = displayName;
         this.hideWhenOffline = hideWhenOffline;
         this.allowWhenOffline = allowWhenOffline;
         this.connectMessage = connectMessage;
         this.offlineMessage = offlineMessage;
         this.iconTemplate = iconTemplate;
      }

      public String getId() {
         return this.id;
      }

      public String getBungeeServer() {
         return this.bungeeServer;
      }

      public String getHost() {
         return this.host;
      }

      public int getPort() {
         return this.port;
      }

      public String getPermission() {
         return this.permission;
      }

      public int getSlot() {
         return this.slot;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public boolean hideWhenOffline() {
         return this.hideWhenOffline;
      }

      public boolean allowWhenOffline() {
         return this.allowWhenOffline;
      }

      public Component connectMessage() {
         return this.connectMessage;
      }

      public Component offlineMessage() {
         return this.offlineMessage;
      }

      public NetworkConfiguration.IconTemplate getIconTemplate() {
         return this.iconTemplate;
      }
   }

   public static final class ServerStatusSnapshot {
      private final NetworkConfiguration.ServerStatusSnapshot.Connectivity connectivity;
      private final long pingMillis;

      private ServerStatusSnapshot(NetworkConfiguration.ServerStatusSnapshot.Connectivity connectivity, long pingMillis) {
         this.connectivity = connectivity;
         this.pingMillis = pingMillis;
      }

      public static NetworkConfiguration.ServerStatusSnapshot online(long pingMillis) {
         return new NetworkConfiguration.ServerStatusSnapshot(NetworkConfiguration.ServerStatusSnapshot.Connectivity.ONLINE, Math.max(1L, pingMillis));
      }

      public static NetworkConfiguration.ServerStatusSnapshot offline() {
         return new NetworkConfiguration.ServerStatusSnapshot(NetworkConfiguration.ServerStatusSnapshot.Connectivity.OFFLINE, -1L);
      }

      public static NetworkConfiguration.ServerStatusSnapshot unknown() {
         return new NetworkConfiguration.ServerStatusSnapshot(NetworkConfiguration.ServerStatusSnapshot.Connectivity.UNKNOWN, -1L);
      }

      public NetworkConfiguration.ServerStatusSnapshot.Connectivity getConnectivity() {
         return this.connectivity;
      }

      public String pingDisplay() {
         return this.connectivity == NetworkConfiguration.ServerStatusSnapshot.Connectivity.ONLINE ? Long.toString(this.pingMillis) : "N/A";
      }

      public String statusMiniMessage() {
         return switch (this.connectivity) {
            case ONLINE -> "<green>Online</green>";
            case OFFLINE -> "<red>Offline</red>";
            case UNKNOWN -> "<gray>Unknown</gray>";
         };
      }

      public boolean isOnline() {
         return this.connectivity == NetworkConfiguration.ServerStatusSnapshot.Connectivity.ONLINE;
      }

      public boolean isOffline() {
         return this.connectivity == NetworkConfiguration.ServerStatusSnapshot.Connectivity.OFFLINE;
      }

      public boolean isUnknown() {
         return this.connectivity == NetworkConfiguration.ServerStatusSnapshot.Connectivity.UNKNOWN;
      }

      public static enum Connectivity {
         ONLINE,
         OFFLINE,
         UNKNOWN;
      }
   }
}
