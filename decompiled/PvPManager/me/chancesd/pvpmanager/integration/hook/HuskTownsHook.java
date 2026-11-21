package me.chancesd.pvpmanager.integration.hook;

import java.util.Optional;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import me.chancesd.pvpmanager.player.ProtectionType;
import net.william278.husktowns.api.BukkitHuskTownsAPI;
import net.william278.husktowns.libraries.cloplib.operation.Operation;
import net.william278.husktowns.libraries.cloplib.operation.OperationType;
import net.william278.husktowns.libraries.cloplib.operation.OperationTypeRegistry;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HuskTownsHook extends BaseDependency implements ForceToggleDependency, RegionDependency {
   private final BukkitHuskTownsAPI huskTownsAPI = BukkitHuskTownsAPI.getInstance();

   public HuskTownsHook(Hook hook) {
      super(hook);
   }

   @Override
   public boolean shouldDisable(Player player) {
      OnlineUser user = this.huskTownsAPI.getOnlineUser(player);
      Optional<Member> member = this.huskTownsAPI.getUserTown(user);
      if (!member.isPresent()) {
         return false;
      } else {
         Town town = member.get().town();
         return town.getCurrentWar().isPresent();
      }
   }

   @Override
   public boolean shouldDisable(Player attacker, Player defender, ProtectionType reason) {
      return this.shouldDisable(attacker) && this.shouldDisable(defender);
   }

   @Override
   public boolean canAttackAt(Player player, Location location) {
      OperationTypeRegistry reg = this.huskTownsAPI.getOperationTypeRegistry();
      boolean cancelled = reg.getHandler().cancelOperation(Operation.of(OperationType.PLAYER_DAMAGE_PLAYER, this.huskTownsAPI.getPosition(location)));
      return !cancelled;
   }
}
