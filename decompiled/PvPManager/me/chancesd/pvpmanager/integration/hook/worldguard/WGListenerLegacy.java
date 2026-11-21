package me.chancesd.pvpmanager.integration.hook.worldguard;

import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.sdutils.utils.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WGListenerLegacy implements Listener {
   private final PlayerManager ph;

   public WGListenerLegacy(PlayerManager ph) {
      this.ph = ph;
   }

   @EventHandler
   public final void onWGPvPCancel(DisallowedPVPEvent event) {
      if (!event.getAttacker().hasMetadata("NPC") && !event.getDefender().hasMetadata("NPC")) {
         if (this.ph.checkProtection(event.getAttacker(), event.getDefender()).type() == ProtectionType.FAIL_OVERRIDE) {
            event.setCancelled(true);
            Log.debug("Force allowing PvP even though WorldGuard blocked it because a player has override or Vulnerable is enabled");
         }
      }
   }
}
