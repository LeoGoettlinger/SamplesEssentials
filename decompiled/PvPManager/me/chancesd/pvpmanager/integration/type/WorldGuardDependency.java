package me.chancesd.pvpmanager.integration.type;

import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Set;
import me.chancesd.pvpmanager.manager.PlayerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface WorldGuardDependency extends GroupDependency, RegionDependency {
   boolean hasAllowPvPFlag(Player var1);

   boolean hasDenyPvPFlag(Player var1);

   State getWGPvPState(Location var1);

   State getStateFlagAt(Location var1, StateFlag var2);

   Set<ProtectedRegion> getRegionsAt(Location var1);

   boolean containsRegionsAt(Location var1, Set<String> var2);

   void startListener(PlayerManager var1);
}
