package com.Sw3ys.bigenderchest.listeners;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import com.Sw3ys.bigenderchest.manager.EnderChestManager;
import com.Sw3ys.bigenderchest.util.Sched;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SearchListener implements Listener {
   private final BiggerEnderChest plugin;
   private final EnderChestManager manager;
   private final Set<UUID> searching = ConcurrentHashMap.newKeySet();

   public SearchListener(BiggerEnderChest plugin, EnderChestManager manager) {
      this.plugin = plugin;
      this.manager = manager;
   }

   public void startSearch(Player player) {
      this.searching.add(player.getUniqueId());
   }

   @EventHandler
   public void onChat(AsyncChatEvent e) {
      Player p = e.getPlayer();
      if (this.searching.contains(p.getUniqueId())) {
         e.setCancelled(true);
         String msg = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();
         if (msg.equalsIgnoreCase("cancel")) {
            this.searching.remove(p.getUniqueId());
            p.sendMessage(ChatColor.RED + "Search cancelled.");
         } else {
            this.searching.remove(p.getUniqueId());
            Sched.entity(this.plugin, p, () -> {
               OfflinePlayer target = Bukkit.getOfflinePlayer(msg);
               if (!target.hasPlayedBefore() && !target.isOnline()) {
                  p.sendMessage(ChatColor.RED + "That player has never joined.");
               } else {
                  this.manager.open(p, target.getUniqueId());
               }
            });
         }
      }
   }
}
