package me.chancesd.pvpmanager.integration.hook;

import com.earth2me.essentials.Essentials;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.AFKDependency;
import me.chancesd.pvpmanager.integration.type.GodDependency;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import net.essentialsx.api.v2.events.TeleportRequestResponseEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EssentialsHook extends BaseDependency implements AFKDependency, GodDependency, Listener {
   private final Essentials ess;

   public EssentialsHook(Hook hook) {
      super(hook);
      this.ess = (Essentials)hook.getPlugin();
      Bukkit.getPluginManager().registerEvents(this, PvPManager.getInstance());
   }

   @EventHandler
   public void onTeleportRequestResponse(TeleportRequestResponseEvent event) {
      if (Conf.BLOCK_TELEPORT.asBool() && !event.isDeny()) {
         Player requester = event.getRequester().getBase();
         Player requestee = event.getRequestee().getBase();
         CombatPlayer combatRequester = CombatPlayer.get(requester);
         CombatPlayer combatRequestee = CombatPlayer.get(requestee);
         boolean requesterInCombat = combatRequester != null && combatRequester.isInCombat();
         boolean requesteeInCombat = combatRequestee != null && combatRequestee.isInCombat();
         if (requesterInCombat || requesteeInCombat) {
            event.setCancelled(true);
            if (requesterInCombat) {
               combatRequester.message(Lang.TELEPORT_BLOCKED_IN_COMBAT, new Object[0]);
            }

            if (requesteeInCombat) {
               combatRequestee.message(Lang.TELEPORT_BLOCKED_IN_COMBAT, new Object[0]);
            }
         }
      }
   }

   @Override
   public boolean hasGodMode(Player player) {
      return this.ess.getUser(player).isGodModeEnabled();
   }

   @Override
   public void enableGodMode(Player player) {
      this.ess.getUser(player).setGodModeEnabled(true);
   }

   @Override
   public void disableGodMode(Player player) {
      this.ess.getUser(player).setGodModeEnabled(false);
   }

   @Override
   public boolean isAFK(Player player) {
      return this.ess.getUser(player).isAfk();
   }
}
