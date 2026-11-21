package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.gui.FactionClaimSelectionGuiManager;
import com.skyblockexp.ezrtp.gui.RandomTeleportGuiManager;
import com.skyblockexp.ezrtp.listener.PlayerJoinTeleportListener;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.network.NetworkService;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import java.util.function.Supplier;
import org.bukkit.plugin.PluginManager;

public final class ListenerRegistrar {
   private final EzRtpPlugin plugin;
   private PlayerJoinTeleportListener joinTeleportListener;
   private RandomTeleportGuiManager guiManager;
   private FactionClaimSelectionGuiManager factionClaimGuiManager;

   public ListenerRegistrar(EzRtpPlugin plugin) {
      this.plugin = plugin;
   }

   public void register(
      Supplier<RandomTeleportService> teleportServiceSupplier,
      Supplier<EzRtpConfiguration> configurationSupplier,
      Supplier<NetworkService> networkServiceSupplier,
      Supplier<MessageProvider> messageProviderSupplier,
      RtpUsageStorage usageStorage
   ) {
      PluginManager pluginManager = this.plugin.getServer().getPluginManager();
      this.joinTeleportListener = new PlayerJoinTeleportListener(
         this.plugin,
         PlatformRuntimeRegistry.get().scheduler(),
         teleportServiceSupplier,
         () -> configurationSupplier.get() != null ? configurationSupplier.get().getDefaultSettings() : null
      );
      this.guiManager = new RandomTeleportGuiManager(
         this.plugin, teleportServiceSupplier, configurationSupplier, networkServiceSupplier, messageProviderSupplier, usageStorage
      );
      this.factionClaimGuiManager = new FactionClaimSelectionGuiManager(
         this.plugin, teleportServiceSupplier, configurationSupplier, messageProviderSupplier, usageStorage
      );
      pluginManager.registerEvents(this.joinTeleportListener, this.plugin);
      pluginManager.registerEvents(this.guiManager, this.plugin);
      pluginManager.registerEvents(this.factionClaimGuiManager, this.plugin);
   }

   public RandomTeleportGuiManager getGuiManager() {
      return this.guiManager;
   }

   public FactionClaimSelectionGuiManager getFactionClaimGuiManager() {
      return this.factionClaimGuiManager;
   }
}
