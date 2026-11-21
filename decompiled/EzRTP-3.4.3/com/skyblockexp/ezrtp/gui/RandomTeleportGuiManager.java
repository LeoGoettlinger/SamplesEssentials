package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.gui.GuiServerOption;
import com.skyblockexp.ezrtp.config.gui.GuiSettings;
import com.skyblockexp.ezrtp.config.gui.GuiWorldOption;
import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;
import com.skyblockexp.ezrtp.config.teleport.RtpLimitSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.network.NetworkService;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.PlaceholderUtil;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomTeleportGuiManager implements Listener {
   private final JavaPlugin plugin;
   private final Supplier<RandomTeleportService> teleportServiceSupplier;
   private final Supplier<EzRtpConfiguration> configurationSupplier;
   private final Supplier<NetworkService> networkServiceSupplier;
   private final RtpUsageStorage usageStorage;
   private final Supplier<MessageProvider> messageSupplier;
   private final GuiSessionManager sessionManager;

   public RandomTeleportGuiManager(
      JavaPlugin plugin,
      Supplier<RandomTeleportService> teleportServiceSupplier,
      Supplier<EzRtpConfiguration> configurationSupplier,
      Supplier<NetworkService> networkServiceSupplier,
      Supplier<MessageProvider> messageSupplier,
      RtpUsageStorage usageStorage
   ) {
      this.plugin = plugin;
      this.teleportServiceSupplier = teleportServiceSupplier;
      this.configurationSupplier = configurationSupplier;
      this.networkServiceSupplier = networkServiceSupplier;
      this.messageSupplier = messageSupplier;
      this.usageStorage = usageStorage;
      this.sessionManager = new GuiSessionManager();
   }

   public boolean openSelection(Player player) {
      EzRtpConfiguration configuration = this.configurationSupplier.get();
      if (configuration == null) {
         return false;
      } else {
         GuiSettings guiSettings = configuration.getGuiSettings();
         if (!guiSettings.isEnabled()) {
            return false;
         } else if (guiSettings.getWorldOptions().isEmpty() && guiSettings.getServerOptions().isEmpty()) {
            return false;
         } else {
            if (!configuration.isAllowGuiDuringCooldown()) {
               boolean onCooldown = this.checkGlobalCooldown(player, configuration);
               if (onCooldown) {
                  MessageUtil.send(player, this.messageSupplier.get().format(MessageKey.GUI_COOLDOWN, player));
                  return false;
               }
            }

            GuiBuilder guiBuilder = this.createGuiBuilder(configuration);
            GuiBuilder.GuiBuildResult result = guiBuilder.buildGui(player);
            if (result.getOptionMap().isEmpty()) {
               MessageUtil.send(player, guiSettings.getNoDestinations());
               return true;
            } else {
               this.sendCacheFilterMessage(player, guiSettings);
               this.sessionManager.startSession(player, result.getInventory(), result.getOptionMap(), result.getSettings());
               player.openInventory(result.getInventory());
               this.refreshInventoryForSpigot(player);
               return true;
            }
         }
      }
   }

   private boolean checkGlobalCooldown(Player player, EzRtpConfiguration configuration) {
      RtpLimitSettings limit = configuration.getLimitSettings(player.getWorld().getName(), null);
      int cooldownSeconds = limit != null ? limit.getCooldownSeconds() : 0;
      if (cooldownSeconds <= 0) {
         return false;
      } else {
         long now = System.currentTimeMillis();
         long lastRtp = this.usageStorage.getLastRtpTime(player.getUniqueId(), null);
         long cooldownMs = cooldownSeconds * 1000L;
         return lastRtp > 0L && now - lastRtp < cooldownMs;
      }
   }

   private void sendCacheFilterMessage(Player player, GuiSettings guiSettings) {
   }

   private void refreshInventoryForSpigot(Player player) {
      if (player != null) {
         try {
            player.updateInventory();
         } catch (Throwable var3) {
         }
      }
   }

   private GuiBuilder createGuiBuilder(EzRtpConfiguration configuration) {
      Logger fallbackLogger = Logger.getLogger(RandomTeleportGuiManager.class.getSimpleName());
      Logger effectiveLogger = this.plugin != null ? this.plugin.getLogger() : fallbackLogger;
      RandomTeleportService teleportService = this.teleportServiceSupplier.get();
      return new GuiBuilder(
         configuration, this.usageStorage, teleportService != null ? teleportService.getBiomeCache() : null, this.networkServiceSupplier.get(), effectiveLogger
      );
   }

   public void closeAll() {
      Map<UUID, GuiSessionManager.GuiSession> sessions = this.sessionManager.getAllSessions();
      if (!sessions.isEmpty() && this.plugin != null) {
         for (UUID playerId : sessions.keySet()) {
            Player active = this.plugin.getServer().getPlayer(playerId);
            if (active != null && active.isOnline()) {
               active.closeInventory();
            }
         }

         this.sessionManager.clearAllSessions();
      } else {
         this.sessionManager.clearAllSessions();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         GuiSessionManager.GuiSession session = this.sessionManager.getSession(player);
         if (session != null && Objects.equals(event.getView().getTopInventory(), session.inventory())) {
            if (event.isShiftClick()) {
               session.settings().noPermissionMessage().ifPresent(msgx -> MessageUtil.send(player, msgx));
               event.setCancelled(true);
               event.setResult(Result.DENY);
            } else {
               event.setCancelled(true);
               event.setResult(Result.DENY);
               if (event.getRawSlot() >= 0 && event.getRawSlot() < session.inventory().getSize()) {
                  GuiOption option = session.options().get(event.getRawSlot());
                  if (option != null) {
                     if (option.isWorldOption()) {
                        GuiWorldOption worldOption = option.worldOption();
                        if (!worldOption.getPermission().isEmpty() && !player.hasPermission(worldOption.getPermission())) {
                           session.settings().noPermissionMessage().ifPresent(msgx -> MessageUtil.send(player, msgx));
                        } else {
                           RandomTeleportService service = this.teleportServiceSupplier.get();
                           if (service != null) {
                              RandomTeleportSettings settings = worldOption.getSettings();
                              EzRtpConfiguration configuration = this.configurationSupplier.get();
                              MessageProvider messages = this.messageSupplier.get();
                              if (configuration != null && messages != null) {
                                 String worldName = settings.isAutoWorld() ? player.getWorld().getName() : settings.getWorldName();
                                 boolean bypass = player.isOp();
                                 if (!bypass) {
                                    for (String perm : configuration.getBypassPermissions()) {
                                       if (player.hasPermission(perm)) {
                                          bypass = true;
                                          break;
                                       }
                                    }
                                 }

                                 if (!bypass) {
                                    String group = configuration.resolveGroup(player, worldName);
                                    RtpLimitSettings limit = configuration.getLimitSettings(worldName, group);
                                    long now = System.currentTimeMillis();
                                    long lastRtp = this.usageStorage.getLastRtpTime(player.getUniqueId(), worldName);
                                    int daily = this.usageStorage.getUsageCount(player.getUniqueId(), worldName, "daily");
                                    int weekly = this.usageStorage.getUsageCount(player.getUniqueId(), worldName, "weekly");
                                    if (limit.getCooldownSeconds() > 0 && lastRtp > 0L && now - lastRtp < limit.getCooldownSeconds() * 1000L) {
                                       long wait = (limit.getCooldownSeconds() * 1000L - (now - lastRtp)) / 1000L;
                                       String msg = messages.getMessage(MessageKey.COOLDOWN);
                                       if (configuration.isHumanReadableCooldown()) {
                                          String formattedTime = PlaceholderUtil.formatTime((int)wait);
                                          String[] parts = formattedTime.split(" ");
                                          String hours = parts.length > 0 && parts[0].endsWith("h") ? parts[0].substring(0, parts[0].length() - 1) : "0";
                                          String minutes = parts.length > 1 && parts[1].endsWith("m") ? parts[1].substring(0, parts[1].length() - 1) : "0";
                                          String seconds = parts.length > 2 && parts[2].endsWith("s")
                                             ? parts[2].substring(0, parts[2].length() - 1)
                                             : String.valueOf(wait);
                                          msg = msg.replace("<hours>", hours).replace("<minutes>", minutes).replace("<seconds>", seconds);
                                       } else {
                                          msg = msg.replace("<seconds>", String.valueOf(wait));
                                       }

                                       String resolvedCooldown = PlaceholderUtil.resolvePlaceholders(player, msg, this.plugin.getLogger());
                                       MessageUtil.send(player, MessageUtil.parseMiniMessage(resolvedCooldown));
                                       return;
                                    }

                                    if (!limit.isDisableDailyLimit() && limit.getDailyLimit() > 0 && daily >= limit.getDailyLimit()) {
                                       String msg = messages.getMessage(MessageKey.LIMIT_DAILY);
                                       MessageUtil.send(
                                          player, MessageUtil.parseMiniMessage(PlaceholderUtil.resolvePlaceholders(player, msg, this.plugin.getLogger()))
                                       );
                                       return;
                                    }

                                    if (!limit.isDisableDailyLimit() && limit.getWeeklyLimit() > 0 && weekly >= limit.getWeeklyLimit()) {
                                       String msg = messages.getMessage(MessageKey.LIMIT_WEEKLY);
                                       MessageUtil.send(
                                          player, MessageUtil.parseMiniMessage(PlaceholderUtil.resolvePlaceholders(player, msg, this.plugin.getLogger()))
                                       );
                                       return;
                                    }
                                 }

                                 player.closeInventory();
                                 service.teleportPlayer(player, settings, TeleportReason.COMMAND, success -> {
                                    if (success && this.usageStorage != null) {
                                       this.usageStorage.setLastRtpTime(player.getUniqueId(), worldName, System.currentTimeMillis());
                                       this.usageStorage.incrementUsage(player.getUniqueId(), worldName, "daily");
                                       this.usageStorage.incrementUsage(player.getUniqueId(), worldName, "weekly");
                                       PlatformRuntimeRegistry.get().scheduler().executeAsync(() -> this.usageStorage.save());
                                    }
                                 });
                              }
                           }
                        }
                     } else {
                        GuiServerOption serverOption = option.serverOption();
                        if (serverOption != null) {
                           NetworkService networkService = this.networkServiceSupplier != null ? this.networkServiceSupplier.get() : null;
                           if (networkService == null) {
                              MessageUtil.send(player, this.messageSupplier.get().getMessage(MessageKey.NETWORK_SERVICE_UNAVAILABLE));
                           } else {
                              NetworkConfiguration.NetworkServer server = serverOption.getServer();
                              if (!server.getPermission().isEmpty() && !player.hasPermission(server.getPermission())) {
                                 session.settings().noPermissionMessage().ifPresent(msgx -> MessageUtil.send(player, msgx));
                              } else {
                                 NetworkConfiguration.ServerStatusSnapshot status = networkService.getStatus(server);
                                 if (status.isOffline() && !server.allowWhenOffline()) {
                                    Component message = server.offlineMessage();
                                    if (message != null && !message.equals(Component.empty())) {
                                       MessageUtil.send(player, message);
                                    } else {
                                       String fallback = null;

                                       try {
                                          MessageProvider provider = this.messageSupplier.get();
                                          if (provider != null) {
                                             fallback = provider.getMessage(MessageKey.NETWORK_SERVER_OFFLINE);
                                          }
                                       } catch (Throwable var29) {
                                       }

                                       if (fallback == null) {
                                          fallback = "§cThat server is currently offline.";
                                       }

                                       try {
                                          player.sendMessage(fallback);
                                       } catch (Throwable var28) {
                                          MessageUtil.send(player, fallback);
                                       }
                                    }
                                 } else {
                                    player.closeInventory();
                                    networkService.transferPlayer(player, server);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         GuiSessionManager.GuiSession session = this.sessionManager.getSession(player);
         if (session != null && Objects.equals(event.getView().getTopInventory(), session.inventory())) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player player) {
         GuiSessionManager.GuiSession session = this.sessionManager.getSession(player);
         if (session != null) {
            if (Objects.equals(session.inventory(), event.getInventory())) {
               this.sessionManager.endSession(player);
            }
         }
      }
   }

   private void fillInventory(Inventory inventory, ItemStack fillerTemplate) {
      ItemStack filler = fillerTemplate.clone();

      for (int slot = 0; slot < inventory.getSize(); slot++) {
         if (inventory.getItem(slot) == null) {
            inventory.setItem(slot, filler.clone());
         }
      }
   }
}
