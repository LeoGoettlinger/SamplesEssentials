package com.magmaguy.elitemobs.api.power;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public interface EliteLuaPowerService {
   default EliteLuaPowerProgram registerLuaPower(Plugin owner, NamespacedKey key, long revision, String source) {
      return this.registerLuaPower(owner, key, revision, source, EliteLuaPowerType.MISCELLANEOUS, null);
   }

   EliteLuaPowerProgram registerLuaPower(Plugin var1, NamespacedKey var2, long var3, String var5, EliteLuaPowerType var6, String var7);

   List<EliteLuaPowerProgram> setPowers(Plugin var1, EliteEntity var2, List<NamespacedKey> var3);

   List<EliteLuaPowerProgram> inspectPowers(EliteEntity var1);

   List<EliteLuaPowerActivity> inspectPowerActivity(EliteEntity var1);

   void registerActionHandler(Plugin var1, NamespacedKey var2, NamespacedKey var3, ElitePowerActionHandler var4);

   void unregisterOwner(Plugin var1);
}
