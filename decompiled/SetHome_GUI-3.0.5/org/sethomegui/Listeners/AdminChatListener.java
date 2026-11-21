package org.sethomegui.Listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.regex.Pattern;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.Managers.AdminGUIManager;
import org.sethomegui.Utils.Utils;

public class AdminChatListener implements Listener {
   private final SetHomeGUI plugin;
   private static final Pattern HEX_CLEAN_PATTERN = Pattern.compile("&#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{6}");

   public AdminChatListener(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onAdminChatSearch(AsyncChatEvent event) {
      Player admin = event.getPlayer();
      if (admin.hasMetadata("admin_search_mode")) {
         event.setCancelled(true);
         String rawQuery = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
         rawQuery = HEX_CLEAN_PATTERN.matcher(rawQuery).replaceAll("");
         rawQuery = ChatColor.stripColor(rawQuery);
         rawQuery = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', rawQuery));
         AdminGUIManager manager = this.plugin.getAdminGUIManager();
         FileConfiguration config = this.plugin.getConfig();
         admin.removeMetadata("admin_search_mode", this.plugin);
         if (rawQuery.equalsIgnoreCase("cancel")) {
            String msg = config.getString("messages.admin.search-cancelled", "&cSearch cancelled. Reopening menu...");
            admin.sendMessage(Utils.color(msg));
         } else if (!rawQuery.isEmpty() && !rawQuery.equalsIgnoreCase("all")) {
            manager.setSearchFilter(admin.getUniqueId(), rawQuery.toLowerCase());
            String msg = config.getString("messages.admin.search-filter-applied", "&aFilter applied: &e%query%");
            admin.sendMessage(Utils.color(msg.replace("%query%", rawQuery)));
         } else {
            manager.setSearchFilter(admin.getUniqueId(), "");
            String msg = config.getString("messages.admin.search-reset", "&aSearch reset. Listing all records...");
            admin.sendMessage(Utils.color(msg));
         }

         manager.setPage(admin.getUniqueId(), 1);
         admin.getScheduler().run(this.plugin, task -> manager.openAdminMenu(admin), null);
      }
   }
}
