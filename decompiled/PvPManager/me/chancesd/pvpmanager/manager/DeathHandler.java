package me.chancesd.pvpmanager.manager;

import java.util.HashMap;
import java.util.Set;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DeathHandler {
   private final PlayerManager playerManager;
   private final WorldGuardDependency wg;

   public DeathHandler(PlayerManager playerManager) {
      this.playerManager = playerManager;
      this.wg = (WorldGuardDependency)playerManager.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
   }

   public void processDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      CombatPlayer combatPlayer = this.playerManager.get(player);
      Player killer = player.getKiller();
      if (combatPlayer.isInCombat()) {
         if (killer != null && !killer.equals(player)) {
            CombatPlayer pKiller = this.playerManager.get(killer);
            this.handlePvPDeath(player, combatPlayer, killer, pKiller, event);
         }

         Set<CombatPlayer> enemies = combatPlayer.getEnemies();
         if (Conf.UNTAG_ON_KILL.asBool()) {
            enemies.forEach(enemy -> enemy.removeEnemy(combatPlayer));
         }

         combatPlayer.untag(UntagReason.DEATH);
      } else if (combatPlayer.wasLastDeathPvP()) {
         combatPlayer.setLastDeathWasPvP(false);
      }

      combatPlayer.resetItemCooldowns();
      if (combatPlayer.hasPvPLogged()) {
         this.handleCombatLogDrops(event, player);
      } else {
         this.handlePlayerDrops(event, player, killer);
      }
   }

   private void handlePvPDeath(Player player, CombatPlayer pvPlayer, Player killer, CombatPlayer pKiller, PlayerDeathEvent event) {
      if (Conf.KILL_ABUSE_ENABLED.asBool() && !pKiller.hasPerm(Permissions.EXEMPT_KILL_ABUSE)) {
         pKiller.addVictim(player);
      }

      if (this.wg == null || !this.wg.containsRegionsAt(killer.getLocation(), Conf.KILLS_WG_EXCLUSIONS.asSet())) {
         if (Conf.MONEY_REWARD.asDouble() > 0.0) {
            pKiller.giveReward(pvPlayer);
         }

         if (Conf.MONEY_PENALTY.asDouble() > 0.0) {
            pvPlayer.applyPenalty();
         }

         if (pKiller.canExecuteKillCommand()) {
            CombatUtils.executeCommands(Conf.COMMANDS_ON_KILL.asList(), killer, killer.getName(), player.getName());
         }

         pvPlayer.setLastDeathWasPvP(true);
         if (Conf.EXP_STEAL.asDouble() > 0.0) {
            int expWon = pKiller.giveExp(pvPlayer);
            event.setDroppedExp(0);
            event.setNewExp(player.getTotalExperience() - expWon);
            pvPlayer.message(Lang.EXP_STOLEN, new Object[]{pKiller.getName(), expWon});
         }
      }
   }

   public void handleCombatLogDrops(PlayerDeathEvent event, Player player) {
      if (!Conf.DROP_EXP.asBool()) {
         this.keepExp(event);
      }

      if (!Conf.DROP_INVENTORY.asBool() && Conf.DROP_ARMOR.asBool()) {
         CombatUtils.fakeItemStackDrop(player, player.getInventory().getArmorContents());
         player.getInventory().setArmorContents(null);
      } else if (Conf.DROP_INVENTORY.asBool() && !Conf.DROP_ARMOR.asBool()) {
         CombatUtils.fakeItemStackDrop(player, player.getInventory().getContents());
         player.getInventory().clear();
      }

      if (!Conf.DROP_INVENTORY.asBool() || !Conf.DROP_ARMOR.asBool()) {
         this.keepInv(event);
      }
   }

   public void handlePlayerDrops(PlayerDeathEvent event, Player player, Player killer) {
      if (!player.equals(killer)) {
         Conf.DropMode dropMode = Conf.PLAYER_DROP_MODE.asEnum(Conf.DropMode.class);
         if (dropMode == null) {
            Conf.PLAYER_DROP_MODE.setEnum(Conf.DropMode.class, "ALWAYS");
         } else {
            switch (dropMode) {
               case DROP:
                  if (killer == null) {
                     this.keepInv(event);
                     this.keepExp(event);
                  }
                  break;
               case KEEP:
                  if (killer != null) {
                     this.keepInv(event);
                     this.keepExp(event);
                  }
                  break;
               case TRANSFER:
                  if (killer != null) {
                     ItemStack[] drops = event.getDrops().toArray(new ItemStack[event.getDrops().size()]);
                     HashMap<Integer, ItemStack> returned = killer.getInventory().addItem(drops);
                     CombatUtils.fakeItemStackDrop(player, returned.values().toArray(new ItemStack[returned.values().size()]));
                     event.getDrops().clear();
                  }
                  break;
               case CLEAR:
                  event.getDrops().clear();
                  event.setDroppedExp(0);
            }
         }
      }
   }

   private void keepInv(PlayerDeathEvent event) {
      event.setKeepInventory(true);
      event.getDrops().clear();
   }

   private void keepExp(PlayerDeathEvent event) {
      event.setKeepLevel(true);
      event.setDroppedExp(0);
   }
}
