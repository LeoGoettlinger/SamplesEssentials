package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.gui.GuiServerOption;
import com.skyblockexp.ezrtp.config.gui.GuiSettings;
import com.skyblockexp.ezrtp.config.gui.GuiWorldOption;
import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;
import com.skyblockexp.ezrtp.config.teleport.RtpLimitSettings;
import com.skyblockexp.ezrtp.network.NetworkService;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiOptionsBuilder {
   private final EzRtpConfiguration configuration;
   private final RtpUsageStorage usageStorage;
   private final BiomeLocationCache biomeCache;
   private final NetworkService networkService;
   private final Player player;
   private final GuiIconFactory iconFactory;

   public GuiOptionsBuilder(
      EzRtpConfiguration configuration,
      RtpUsageStorage usageStorage,
      BiomeLocationCache biomeCache,
      NetworkService networkService,
      Player player,
      Logger logger
   ) {
      this.configuration = configuration;
      this.usageStorage = usageStorage;
      this.biomeCache = biomeCache;
      this.networkService = networkService;
      this.player = player;
      this.iconFactory = new GuiIconFactory(logger);
   }

   public Map<Integer, GuiOption> buildOptions(Inventory inventory) {
      Map<Integer, GuiOption> optionMap = new HashMap<>();
      GuiSettings guiSettings = this.configuration.getGuiSettings();
      this.addWorldOptions(inventory, optionMap, guiSettings);
      this.addServerOptions(inventory, optionMap, guiSettings);
      return optionMap;
   }

   private void addWorldOptions(Inventory inventory, Map<Integer, GuiOption> optionMap, GuiSettings guiSettings) {
      for (GuiWorldOption option : guiSettings.getWorldOptions()) {
         if (option.getPermission().isEmpty() || this.player.hasPermission(option.getPermission())) {
            String worldForChecks = option.getSettings().isAutoWorld() ? this.player.getWorld().getName() : option.getSettings().getWorldName();
            boolean bypass = this.hasBypass(this.player, worldForChecks);
            GuiIconFactory.CooldownInfo cooldownInfo = bypass ? GuiIconFactory.CooldownInfo.inactive() : this.evaluateCooldown(option);
            if ((!cooldownInfo.active() || this.configuration.isAllowGuiDuringCooldown())
               && (guiSettings.isDisableCacheFiltering() || !option.isRequireCacheEnabled() || this.meetsCacheRequirements(option))) {
               int slot = option.getSlot();
               if (slot >= 0 && slot < inventory.getSize() && !optionMap.containsKey(slot)) {
                  ItemStack icon = this.iconFactory.buildWorldIcon(this.player, option, cooldownInfo, this.configuration.isHumanReadableCooldown());
                  inventory.setItem(slot, icon);
                  optionMap.put(slot, GuiOption.world(option));
               }
            }
         }
      }
   }

   private GuiIconFactory.CooldownInfo evaluateCooldown(GuiWorldOption option) {
      String worldName = option.getSettings().isAutoWorld() ? this.player.getWorld().getName() : option.getSettings().getWorldName();
      RtpLimitSettings limit = this.configuration.getLimitSettings(worldName, null);
      if (limit != null && limit.getCooldownSeconds() > 0) {
         long lastRtp = this.usageStorage.getLastRtpTime(this.player.getUniqueId(), worldName);
         if (lastRtp <= 0L) {
            return GuiIconFactory.CooldownInfo.inactive();
         } else {
            long elapsed = System.currentTimeMillis() - lastRtp;
            long remainingMillis = limit.getCooldownSeconds() * 1000L - elapsed;
            if (remainingMillis <= 0L) {
               return GuiIconFactory.CooldownInfo.inactive();
            } else {
               long remainingSeconds = (long)Math.ceil(remainingMillis / 1000.0);
               return new GuiIconFactory.CooldownInfo(true, remainingSeconds);
            }
         }
      } else {
         return GuiIconFactory.CooldownInfo.inactive();
      }
   }

   private boolean hasBypass(Player player, String worldName) {
      if (player.isOp()) {
         return true;
      } else {
         for (String perm : this.configuration.getBypassPermissions()) {
            if (player.hasPermission(perm)) {
               return true;
            }
         }

         String group = this.configuration.resolveGroup(player, worldName);
         RtpLimitSettings limit = this.configuration.getLimitSettings(worldName, group);
         return limit != null && limit.isDisableDailyLimit();
      }
   }

   private boolean meetsCacheRequirements(GuiWorldOption option) {
      if (this.biomeCache == null) {
         return false;
      } else {
         String worldName = option.getSettings().isAutoWorld() ? this.player.getWorld().getName() : option.getSettings().getWorldName();
         return this.biomeCache.getCachedLocationCount(Bukkit.getWorld(worldName), option.getSettings().getBiomeInclude()) >= option.getMinimumCached();
      }
   }

   private void addServerOptions(Inventory inventory, Map<Integer, GuiOption> optionMap, GuiSettings guiSettings) {
      if (this.networkService != null && this.configuration.getNetworkConfiguration().isLobbyServer()) {
         for (GuiServerOption serverOption : guiSettings.getServerOptions()) {
            NetworkConfiguration.NetworkServer server = serverOption.getServer();
            if (server.getPermission().isEmpty() || this.player.hasPermission(server.getPermission())) {
               int slot = server.getSlot();
               if (slot >= 0 && slot < inventory.getSize() && !optionMap.containsKey(slot)) {
                  NetworkConfiguration.ServerStatusSnapshot status = this.networkService.getStatus(server);
                  if (!server.hideWhenOffline() || !status.isOffline()) {
                     ItemStack icon = this.iconFactory.buildServerIcon(serverOption, status, this.networkService);
                     inventory.setItem(slot, icon);
                     optionMap.put(slot, GuiOption.server(serverOption));
                  }
               }
            }
         }
      }
   }
}
