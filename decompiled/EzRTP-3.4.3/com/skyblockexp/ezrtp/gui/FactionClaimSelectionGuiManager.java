package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.gui.FactionGuiSettings;
import com.skyblockexp.ezrtp.config.teleport.RtpLimitSettings;
import com.skyblockexp.ezrtp.integration.TeamsApiClaimFetcher;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.PluginMessageHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class FactionClaimSelectionGuiManager implements Listener {
   private final EzRtpPlugin plugin;
   private final Supplier<RandomTeleportService> teleportServiceSupplier;
   private final Supplier<EzRtpConfiguration> configurationSupplier;
   private final Supplier<MessageProvider> messageSupplier;
   private final RtpUsageStorage usageStorage;
   private final Map<UUID, FactionClaimSelectionGuiManager.Session> sessions = new HashMap<>();

   public FactionClaimSelectionGuiManager(
      EzRtpPlugin plugin,
      Supplier<RandomTeleportService> teleportServiceSupplier,
      Supplier<EzRtpConfiguration> configurationSupplier,
      Supplier<MessageProvider> messageSupplier,
      RtpUsageStorage usageStorage
   ) {
      this.plugin = plugin;
      this.teleportServiceSupplier = teleportServiceSupplier;
      this.configurationSupplier = configurationSupplier;
      this.messageSupplier = messageSupplier;
      this.usageStorage = usageStorage;
   }

   public boolean openSelection(Player player) {
      if (player == null) {
         return true;
      } else if (!this.plugin.getServer().getPluginManager().isPluginEnabled("TeamsAPI")) {
         MessageUtil.send(player, "<red>TeamsAPI is not available on this server.</red>");
         return true;
      } else {
         EzRtpConfiguration configuration = this.configurationSupplier.get();
         if (configuration != null && configuration.getFactionGuiSettings().enabled()) {
            try {
               Optional<TeamsApiClaimFetcher.FetchResult> fetchResult = TeamsApiClaimFetcher.fetch(player);
               if (fetchResult.isEmpty()) {
                  return true;
               }

               TeamsApiClaimFetcher.FetchResult data = fetchResult.get();
               this.openPage(player, new FactionClaimSelectionGuiManager.Session(data.claims(), data.teamDisplayName(), data.teamId(), 0));
            } catch (NoClassDefFoundError var5) {
               MessageUtil.send(player, "<red>TeamsAPI is not available on this server.</red>");
            }

            return true;
         } else {
            MessageUtil.send(player, "<red>Faction RTP GUI is disabled.</red>");
            return true;
         }
      }
   }

   public void closeAll() {
      for (UUID playerId : new ArrayList<>(this.sessions.keySet())) {
         Player player = this.plugin.getServer().getPlayer(playerId);
         if (player != null && player.isOnline()) {
            player.closeInventory();
         }
      }

      this.sessions.clear();
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         FactionClaimSelectionGuiManager.Session session = this.sessions.get(player.getUniqueId());
         if (session != null && event.getView().getTopInventory().equals(session.inventory)) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < session.settings.size()) {
               if (slot == session.settings.previousSlot() && session.page > 0) {
                  this.openPage(player, new FactionClaimSelectionGuiManager.Session(session.claims, session.teamDisplayName, session.teamId, session.page - 1));
               } else if (slot == session.settings.nextSlot() && (session.page + 1) * session.claimsPerPage() < session.claims.size()) {
                  this.openPage(player, new FactionClaimSelectionGuiManager.Session(session.claims, session.teamDisplayName, session.teamId, session.page + 1));
               } else if (slot < session.claimsPerPage()) {
                  int claimIndex = session.page * session.claimsPerPage() + slot;
                  if (claimIndex >= 0 && claimIndex < session.claims.size()) {
                     ClaimSnapshot claim = session.claims.get(claimIndex);
                     this.teleportToClaimCenter(player, claim);
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
         FactionClaimSelectionGuiManager.Session session = this.sessions.get(player.getUniqueId());
         if (session != null && event.getView().getTopInventory().equals(session.inventory)) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player player) {
         FactionClaimSelectionGuiManager.Session session = this.sessions.get(player.getUniqueId());
         if (session != null && session.inventory.equals(event.getInventory())) {
            this.sessions.remove(player.getUniqueId());
         }
      }
   }

   private void openPage(Player player, FactionClaimSelectionGuiManager.Session session) {
      EzRtpConfiguration configuration = this.configurationSupplier.get();
      FactionGuiSettings settings = configuration != null ? configuration.getFactionGuiSettings() : FactionGuiSettings.fromConfiguration(null);
      session.settings = settings;
      int page = session.page;
      int claimsPerPage = session.claimsPerPage();
      int totalPages = Math.max(1, (int)Math.ceil((double)session.claims.size() / claimsPerPage));
      Inventory inventory = Bukkit.createInventory(null, settings.size(), this.format(settings.title(), page + 1, totalPages));
      int start = page * claimsPerPage;
      int end = Math.min(start + claimsPerPage, session.claims.size());

      for (int i = start; i < end; i++) {
         ClaimSnapshot claim = session.claims.get(i);
         inventory.setItem(i - start, this.createClaimItem(claim, i + 1, settings));
      }

      if (page > 0) {
         inventory.setItem(settings.previousSlot(), this.createNavItem(Material.ARROW, settings.previousName()));
      }

      if (end < session.claims.size()) {
         inventory.setItem(settings.nextSlot(), this.createNavItem(Material.ARROW, settings.nextName()));
      }

      session.inventory = inventory;
      this.sessions.put(player.getUniqueId(), session);
      player.openInventory(inventory);
   }

   private void teleportToClaimCenter(Player player, ClaimSnapshot claim) {
      World claimWorld = this.plugin.getServer().getWorld(claim.worldName());
      if (claimWorld == null) {
         MessageUtil.send(player, "<red>Claim world '<white>" + claim.worldName() + "</white>' is not loaded.</red>");
      } else {
         EzRtpConfiguration configuration = this.configurationSupplier.get();
         RandomTeleportService service = this.teleportServiceSupplier.get();
         MessageProvider messages = this.messageSupplier.get();
         if (configuration != null && service != null && messages != null) {
            RandomTeleportSettings baseSettings = configuration.getSettingsForWorld(claim.worldName());
            if (baseSettings == null) {
               MessageUtil.send(player, "<red>No RTP settings found for world '<white>" + claim.worldName() + "</white>'.</red>");
            } else {
               String worldName = claim.worldName();
               String group = configuration.resolveGroup(player, worldName);
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
                  RtpLimitSettings limit = configuration.getLimitSettings(worldName, group);
                  long now = System.currentTimeMillis();
                  long lastRtp = this.usageStorage.getLastRtpTime(player.getUniqueId(), worldName);
                  int daily = this.usageStorage.getUsageCount(player.getUniqueId(), worldName, "daily");
                  int weekly = this.usageStorage.getUsageCount(player.getUniqueId(), worldName, "weekly");
                  if (limit.getCooldownSeconds() > 0 && lastRtp > 0L && now - lastRtp < limit.getCooldownSeconds() * 1000L) {
                     long wait = (limit.getCooldownSeconds() * 1000L - (now - lastRtp)) / 1000L;
                     PluginMessageHelper.sendCooldownMessage(player, this.plugin, configuration, wait);
                     return;
                  }

                  if (!limit.isDisableDailyLimit() && limit.getDailyLimit() > 0 && daily >= limit.getDailyLimit()) {
                     MessageUtil.send(player, MessageUtil.parseMiniMessage(messages.getMessage(MessageKey.LIMIT_DAILY)));
                     return;
                  }

                  if (!limit.isDisableDailyLimit() && limit.getWeeklyLimit() > 0 && weekly >= limit.getWeeklyLimit()) {
                     MessageUtil.send(player, MessageUtil.parseMiniMessage(messages.getMessage(MessageKey.LIMIT_WEEKLY)));
                     return;
                  }
               }

               MemoryConfiguration override = new MemoryConfiguration();
               override.set("world", worldName);
               override.set("center.x", claim.chunkX() * 16 + 8);
               override.set("center.z", claim.chunkZ() * 16 + 8);
               RandomTeleportSettings claimCentered = RandomTeleportSettings.fromConfiguration(override, this.plugin.getLogger(), baseSettings);
               player.closeInventory();
               service.teleportPlayer(player, claimCentered, TeleportReason.COMMAND, success -> {
                  if (success) {
                     this.usageStorage.setLastRtpTime(player.getUniqueId(), worldName, System.currentTimeMillis());
                     this.usageStorage.incrementUsage(player.getUniqueId(), worldName, "daily");
                     this.usageStorage.incrementUsage(player.getUniqueId(), worldName, "weekly");
                     PlatformRuntimeRegistry.get().scheduler().executeAsync(this.usageStorage::save);
                  }
               });
            }
         } else {
            MessageUtil.send(player, "<red>EzRTP is not fully initialized.</red>");
         }
      }
   }

   private ItemStack createClaimItem(ClaimSnapshot claim, int index, FactionGuiSettings settings) {
      ItemStack item = this.createClaimIcon(claim, settings);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(this.replaceClaimPlaceholders(settings.claimNameFormat(), claim, index));
         List<String> lore = new ArrayList<>();

         for (String line : settings.claimLore()) {
            lore.add(this.replaceClaimPlaceholders(line, claim, index));
         }

         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }

   private ItemStack createClaimIcon(ClaimSnapshot claim, FactionGuiSettings settings) {
      if (!settings.skullEnabled()) {
         return new ItemStack(settings.fallbackMaterial());
      } else {
         UUID ownerUuid = claim.ownerUuid();
         if (ownerUuid == null) {
            return new ItemStack(settings.fallbackMaterial());
         } else {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            if (skull.getItemMeta() instanceof SkullMeta skullMeta) {
               OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(ownerUuid);
               skullMeta.setOwningPlayer(offlinePlayer);
               skull.setItemMeta(skullMeta);
               return skull;
            } else {
               return new ItemStack(settings.fallbackMaterial());
            }
         }
      }
   }

   private ItemStack createNavItem(Material material, String title) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(title);
         item.setItemMeta(meta);
      }

      return item;
   }

   private String replaceClaimPlaceholders(String text, ClaimSnapshot claim, int index) {
      return text.replace("<index>", String.valueOf(index))
         .replace("<world>", claim.worldName())
         .replace("<chunk_x>", String.valueOf(claim.chunkX()))
         .replace("<chunk_z>", String.valueOf(claim.chunkZ()))
         .replace("<center_x>", String.valueOf(claim.chunkX() * 16 + 8))
         .replace("<center_z>", String.valueOf(claim.chunkZ() * 16 + 8));
   }

   private String format(String titleTemplate, int page, int pages) {
      return titleTemplate.replace("<page>", String.valueOf(page)).replace("<pages>", String.valueOf(pages));
   }

   private static final class Session {
      private final List<ClaimSnapshot> claims;
      private final String teamDisplayName;
      private final UUID teamId;
      private final int page;
      private FactionGuiSettings settings;
      private Inventory inventory;

      private Session(List<ClaimSnapshot> claims, String teamDisplayName, UUID teamId, int page) {
         this.claims = claims;
         this.teamDisplayName = teamDisplayName;
         this.teamId = teamId;
         this.page = page;
      }

      private int claimsPerPage() {
         int reserved = 9;
         return Math.max(1, this.settings.size() - reserved);
      }
   }
}
