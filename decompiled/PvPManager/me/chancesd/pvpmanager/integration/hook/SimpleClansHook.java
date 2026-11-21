package me.chancesd.pvpmanager.integration.hook;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.GroupDependency;
import me.chancesd.pvpmanager.player.ProtectionType;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.entity.Player;

public class SimpleClansHook extends BaseDependency implements GroupDependency, ForceToggleDependency {
   private final ClanManager clanManager = ((SimpleClans)this.getPlugin()).getClanManager();

   public SimpleClansHook(Hook hook) {
      super(hook);
      if (this.clanManager == null) {
         throw new DependencyException("Failed to enable SimpleClans support, try updating SimpleClans or check for errors in SimpleClans startup", hook);
      }
   }

   @Override
   public final boolean canAttack(Player attacker, Player defender) {
      ClanPlayer cAttacker = this.clanManager.getClanPlayer(attacker);
      return cAttacker == null || !cAttacker.getClan().isMember(defender) && !cAttacker.isAlly(defender);
   }

   @Override
   public boolean shouldDisable(Player player) {
      ClanPlayer cPlayer = this.clanManager.getClanPlayer(player);
      return cPlayer != null && !cPlayer.getClan().getWarringClans().isEmpty();
   }

   @Override
   public boolean shouldDisable(Player attacker, Player defender, ProtectionType reason) {
      ClanPlayer cAttacker = this.clanManager.getClanPlayer(attacker);
      ClanPlayer cDefender = this.clanManager.getClanPlayer(defender);
      return cAttacker != null && cDefender != null && cAttacker.getClan().isWarring(cDefender.getClan());
   }
}
