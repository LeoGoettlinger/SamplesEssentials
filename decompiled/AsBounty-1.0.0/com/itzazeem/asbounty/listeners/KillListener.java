package com.itzazeem.asbounty.listeners;

import com.itzazeem.asbounty.AsBounty;
import com.itzazeem.asbounty.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillListener implements Listener {
   private final AsBounty plugin;

   public KillListener(AsBounty plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player victim = event.getEntity();
      Player killer = victim.getKiller();
      if (killer != null) {
         double bounty = this.plugin.getBountyManager().getBounty(victim.getUniqueId());
         if (bounty > 0.0 && (!killer.equals(victim) || this.plugin.getConfigManager().isKillMyselfGetReward())) {
            this.plugin.getBountyManager().claimBounty(victim.getUniqueId(), killer.getUniqueId()).thenAccept(claimedAmount -> {
               if (claimedAmount > 0.0) {
                  if (this.plugin.hasEconomy()) {
                     this.plugin.getEconomy().depositPlayer(killer, claimedAmount);
                  }

                  String msg = this.plugin.getConfigManager().getMessage("u-got-bounty-kill").replace("%user_name%", victim.getName());
                  MessageUtils.sendMessageWithPrefix(killer, msg, this.plugin.getConfigManager().isPrefixEnabled(), this.plugin.getConfigManager().getPrefix());
                  MessageUtils.sendActionBar(killer, this.plugin.getConfigManager().getActionBar("u-got-bounty-kill").replace("%user_name%", victim.getName()));
                  this.plugin.getSoundManager().playSound(killer, "bounty-added");
               }
            });
         }
      }
   }
}
