package simplexity.simpleplayerfreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {
   @EventHandler
   public void onLeave(PlayerQuitEvent event) {
      JoinListener.notifyList.remove(event.getPlayer());
      JoinListener.spyList.remove(event.getPlayer());
   }
}
