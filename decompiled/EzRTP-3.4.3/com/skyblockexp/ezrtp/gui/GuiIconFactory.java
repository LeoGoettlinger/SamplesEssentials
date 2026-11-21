package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.gui.GuiServerOption;
import com.skyblockexp.ezrtp.config.gui.GuiWorldOption;
import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;
import com.skyblockexp.ezrtp.network.NetworkService;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridge;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridgeRegistry;
import com.skyblockexp.ezrtp.util.ItemFlagUtil;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.PlaceholderUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class GuiIconFactory {
   private final Logger logger;

   public GuiIconFactory(Logger logger) {
      this.logger = Objects.requireNonNull(logger, "logger");
   }

   public ItemStack buildWorldIcon(Player player, GuiWorldOption option, GuiIconFactory.CooldownInfo cooldownInfo, boolean humanReadableCooldown) {
      ItemStack icon = this.safeClone(option.createIcon());
      ItemMeta meta = icon.getItemMeta();
      if (meta == null) {
         return icon;
      } else {
         PlatformGuiBridge guiBridge = PlatformGuiBridgeRegistry.get();
         String rawDisplayName = option.getRawDisplayName();
         if (rawDisplayName != null && !rawDisplayName.isBlank()) {
            Component displayName = this.parseLine(rawDisplayName, player);
            if (displayName != null && !Component.empty().equals(displayName)) {
               guiBridge.setDisplayName(meta, displayName);
            }
         }

         List<Component> lore = new ArrayList<>();
         if (cooldownInfo != null && cooldownInfo.active()) {
            lore.add(this.buildCooldownComponent(cooldownInfo.remainingSeconds(), humanReadableCooldown));
         }

         for (String raw : option.getRawLore()) {
            Component line = this.parseLine(raw, player);
            if (line != null) {
               lore.add(line);
            }
         }

         if (!lore.isEmpty()) {
            guiBridge.setLore(meta, lore);
         }

         ItemFlagUtil.setItemMetaCompatibly(icon, meta);
         guiBridge.applyItemMeta(icon, meta);
         return icon;
      }
   }

   public ItemStack buildServerIcon(GuiServerOption serverOption, NetworkConfiguration.ServerStatusSnapshot status, NetworkService networkService) {
      NetworkConfiguration.NetworkServer server = serverOption.getServer();
      NetworkConfiguration.ServerStatusSnapshot safeStatus = status != null ? status : NetworkConfiguration.ServerStatusSnapshot.unknown();
      ItemStack icon = networkService != null
         ? this.safeClone(networkService.createIcon(server))
         : this.safeClone(server.getIconTemplate().createIcon(safeStatus, server.getDisplayName()));
      ItemMeta meta = icon.getItemMeta();
      if (meta == null) {
         return icon;
      } else {
         PlatformGuiBridge guiBridge = PlatformGuiBridgeRegistry.get();
         List<Component> lore = this.readLore(meta);
         Component badge = this.buildStatusBadge(server, safeStatus);
         if (badge != null) {
            lore.add(0, badge);
         }

         guiBridge.setLore(meta, lore);
         ItemFlagUtil.setItemMetaCompatibly(icon, meta);
         guiBridge.applyItemMeta(icon, meta);
         return icon;
      }
   }

   private ItemStack safeClone(ItemStack original) {
      return original != null ? original.clone() : new ItemStack(Material.BARRIER);
   }

   private Component parseLine(String raw, Player player) {
      if (raw != null && !raw.isBlank()) {
         try {
            Component parsed = MessageUtil.parseMiniMessage(raw);
            Component resolvedComp = PlaceholderUtil.resolvePlaceholdersComponent(player, parsed, this.logger);
            if (resolvedComp != null && !Component.empty().equals(resolvedComp)) {
               return resolvedComp;
            }
         } catch (Throwable var5) {
         }

         String resolved = PlaceholderUtil.resolvePlaceholders(player, raw, this.logger);
         return (Component)(resolved != null && !resolved.isBlank() ? MessageUtil.parseMiniMessage(resolved) : Component.empty());
      } else {
         return Component.empty();
      }
   }

   private Component buildCooldownComponent(long remainingSeconds, boolean humanReadable) {
      long safeSeconds = Math.max(0L, remainingSeconds);
      String text = humanReadable ? PlaceholderUtil.formatTime((int)Math.min(2147483647L, safeSeconds)) : safeSeconds + "s";
      return MessageUtil.parseMiniMessage("<red>⏰ On cooldown: " + text + "</red>");
   }

   private Component buildStatusBadge(NetworkConfiguration.NetworkServer server, NetworkConfiguration.ServerStatusSnapshot status) {
      if (status.isOffline()) {
         Component offlineMessage = server.offlineMessage();
         return offlineMessage != null && !offlineMessage.equals(Component.empty())
            ? offlineMessage
            : MessageUtil.parseMiniMessage("<red>Currently offline</red>");
      } else {
         return status.isUnknown()
            ? MessageUtil.parseMiniMessage("<yellow>Checking status...</yellow>")
            : MessageUtil.parseMiniMessage("<gray>Latency: <green>" + status.pingDisplay() + " ms</green></gray>");
      }
   }

   private List<Component> readLore(ItemMeta meta) {
      try {
         List<Component> lore = meta.lore();
         if (lore != null) {
            return new ArrayList<>(lore);
         }
      } catch (NoClassDefFoundError | NoSuchMethodError var6) {
      }

      List<String> legacyLore = meta.getLore();
      if (legacyLore != null && !legacyLore.isEmpty()) {
         List<Component> components = new ArrayList<>(legacyLore.size());

         for (String line : legacyLore) {
            components.add(MessageUtil.parseMiniMessage(line));
         }

         return components;
      } else {
         return new ArrayList<>();
      }
   }

   public record CooldownInfo(boolean active, long remainingSeconds) {
      public static GuiIconFactory.CooldownInfo inactive() {
         return new GuiIconFactory.CooldownInfo(false, 0L);
      }
   }
}
