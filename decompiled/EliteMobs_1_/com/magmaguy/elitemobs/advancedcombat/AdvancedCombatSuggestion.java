package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class AdvancedCombatSuggestion implements Listener {
   private static final long REMINDER_DELAY_TICKS = 100L;
   private static final String DEVELOPER_MESSAGE_URL = "https://www.patreon.com/posts/169211257";

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      final Player player = event.getPlayer();
      if (this.shouldSuggest(player)) {
         (new BukkitRunnable() {
            public void run() {
               if (player.isOnline() && AdvancedCombatSuggestion.this.shouldSuggest(player)) {
                  AdvancedCombatSuggestion.sendSuggestion(player);
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, 100L);
      }
   }

   private boolean shouldSuggest(Player player) {
      return AdvancedCombatSystemConfig.isShowDeveloperMessage() && player.hasPermission("elitemobs.advancedcombat.admin");
   }

   private static void sendSuggestion(Player player) {
      player.spigot()
         .sendMessage(
            new BaseComponent[]{
               SpigotMessage.simpleMessage("&6[EliteMobs] &fA message from the MagmaGuy, the developer about the new combat system. "),
               SpigotMessage.hoverLinkMessage("&a[Click to read!]", "&7Read MagmaGuy's free Patreon post", "https://www.patreon.com/posts/169211257"),
               SpigotMessage.simpleMessage(" "),
               SpigotMessage.commandHoverMessage(
                  "&c[Dismiss permanently]", "&7Stop showing this message to all administrators on this server", "/em advancedcombat dismiss"
               )
            }
         );
   }
}
