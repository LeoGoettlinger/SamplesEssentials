package eu.kennytv.maintenance.paper.listener;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.paper.MaintenancePaperPlugin;
import eu.kennytv.maintenance.paper.util.PaperSenderInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public final class PlayerLoginListener extends JoinListenerBase implements Listener {
   private final MaintenancePaperPlugin plugin;

   public PlayerLoginListener(MaintenancePaperPlugin plugin, Settings settings) {
      super(plugin, settings);
      this.plugin = plugin;
   }

   @EventHandler
   public void postLogin(PlayerLoginEvent event) {
      SenderInfo sender = new PaperSenderInfo(event.getPlayer());
      if (this.shouldKick(sender)) {
         event.setResult(Result.KICK_OTHER);
         event.kickMessage(this.settings.getKickMessage());
         if (this.settings.isJoinNotifications()) {
            this.broadcastJoinNotification(sender.name());
         }
      }
   }

   @Override
   protected void broadcastJoinNotification(String name) {
      for (Player p : this.plugin.getServer().getOnlinePlayers()) {
         if (this.plugin.hasPermission(p, "joinnotification")) {
            p.sendMessage(this.settings.getMessage("joinNotification", "%PLAYER%", name));
         }
      }
   }
}
