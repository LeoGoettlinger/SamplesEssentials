package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.gui.GuiSettings;
import com.skyblockexp.ezrtp.network.NetworkService;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridgeRegistry;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiBuilder {
   private final EzRtpConfiguration configuration;
   private final RtpUsageStorage usageStorage;
   private final BiomeLocationCache biomeCache;
   private final NetworkService networkService;
   private final Logger logger;
   private static final InventoryHolder DUMMY_HOLDER = new InventoryHolder() {
      public Inventory getInventory() {
         return null;
      }
   };

   public GuiBuilder(
      EzRtpConfiguration configuration, RtpUsageStorage usageStorage, BiomeLocationCache biomeCache, NetworkService networkService, Logger logger
   ) {
      this.configuration = configuration;
      this.usageStorage = usageStorage;
      this.biomeCache = biomeCache;
      this.networkService = networkService;
      this.logger = logger;
   }

   public GuiBuilder.GuiBuildResult buildGui(Player player) {
      GuiSettings guiSettings = this.configuration.getGuiSettings();
      Inventory inventory = this.createInventory(guiSettings);
      GuiOptionsBuilder optionsBuilder = new GuiOptionsBuilder(this.configuration, this.usageStorage, this.biomeCache, this.networkService, player, this.logger);
      Map<Integer, GuiOption> optionMap = optionsBuilder.buildOptions(inventory);
      return new GuiBuilder.GuiBuildResult(inventory, optionMap, guiSettings);
   }

   private Inventory createInventory(GuiSettings guiSettings) {
      return PlatformGuiBridgeRegistry.get().createInventory(DUMMY_HOLDER, guiSettings.getSize(), guiSettings.getTitle());
   }

   public static class GuiBuildResult {
      private final Inventory inventory;
      private final Map<Integer, GuiOption> optionMap;
      private final GuiSettings settings;

      public GuiBuildResult(Inventory inventory, Map<Integer, GuiOption> optionMap, GuiSettings settings) {
         this.inventory = inventory;
         this.optionMap = optionMap;
         this.settings = settings;
      }

      public Inventory getInventory() {
         return this.inventory;
      }

      public Map<Integer, GuiOption> getOptionMap() {
         return this.optionMap;
      }

      public GuiSettings getSettings() {
         return this.settings;
      }
   }
}
