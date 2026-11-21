package eu.kennytv.maintenance.core.listener;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.ComponentUtil;
import eu.kennytv.maintenance.core.util.DiscordWebhook;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class JoinListenerBase {
   protected final MaintenancePlugin plugin;
   protected final Settings settings;
   private final Set<UUID> notifiedPlayers = new HashSet<>();

   protected JoinListenerBase(MaintenancePlugin plugin, Settings settings) {
      this.plugin = plugin;
      this.settings = settings;
   }

   protected boolean shouldKick(SenderInfo sender, boolean updateCheck) {
      if (this.settings.isMaintenance() && !sender.hasMaintenancePermission("bypass") && !this.settings.isWhitelisted(sender.uuid())) {
         if (this.settings.isWebhookEnabled()) {
            this.plugin.sendWebhookMessage("webhookJoinNotification", DiscordWebhook.EventType.BLOCKED_JOIN, "%PLAYER%", sender.name());
         }

         return true;
      } else {
         if (updateCheck) {
            this.updateCheck(sender);
         }

         return false;
      }
   }

   protected boolean shouldKick(SenderInfo sender) {
      return this.shouldKick(sender, true);
   }

   protected void updateCheck(SenderInfo sender) {
      if (this.settings.hasUpdateChecks()) {
         if (sender.hasPermission("maintenance.admin") && !this.notifiedPlayers.contains(sender.uuid())) {
            this.plugin
               .async(
                  () -> {
                     if (this.plugin.updateAvailable()) {
                        this.notifiedPlayers.add(sender.uuid());
                        sender.sendPrefixedRich(
                           "<red>There is a newer version available: <green>Version "
                              + this.plugin.getNewestVersion()
                              + "<red>, you're on <green>"
                              + this.plugin.getVersion()
                        );
                        TextComponent text = (TextComponent)((Builder)((Builder)((Builder)((Builder)((Builder)Component.text()
                                          .content("Download it at: ")
                                          .color(NamedTextColor.RED))
                                       .append(Component.text().content("https://hangar.papermc.io/kennytv/Maintenance").color(NamedTextColor.GOLD)))
                                    .append(((Builder)Component.text().content(" (CLICK ME)").color(NamedTextColor.GRAY)).decorate(TextDecoration.BOLD)))
                                 .clickEvent(ComponentUtil.clickEvent(Action.OPEN_URL, "https://hangar.papermc.io/kennytv/Maintenance")))
                              .hoverEvent(HoverEvent.showText(Component.text("Download the latest version").color(NamedTextColor.GREEN))))
                           .build();
                        sender.send(this.plugin.prefix().append(text));
                     }
                  }
               );
         }
      }
   }

   protected abstract void broadcastJoinNotification(String var1);
}
