package me.chancesd.pvpmanager.integration.hook;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.libraries.cloplib.operation.Operation;
import net.william278.huskclaims.libraries.cloplib.operation.OperationType;
import net.william278.huskclaims.libraries.cloplib.operation.OperationTypeRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HuskClaimsHook extends BaseDependency implements RegionDependency {
   private final BukkitHuskClaimsAPI huskClaimsAPI = BukkitHuskClaimsAPI.getInstance();

   public HuskClaimsHook(Hook hook) {
      super(hook);
   }

   @Override
   public boolean canAttackAt(Player player, Location location) {
      OperationTypeRegistry reg = this.huskClaimsAPI.getOperationTypeRegistry();
      boolean cancelled = reg.getHandler().cancelOperation(Operation.of(OperationType.PLAYER_DAMAGE_PLAYER, this.huskClaimsAPI.getPosition(location)));
      return !cancelled;
   }
}
