package com.magmaguy.elitemobs.api.mind;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public interface EliteMindService {
   EliteMindModule registerLuaModule(Plugin var1, NamespacedKey var2, long var3, String var5);

   EliteMindProgram registerLuaProgram(Plugin var1, NamespacedKey var2, long var3, String var5);

   void registerNaturalSpawnProvider(Plugin var1, EliteNaturalSpawnProvider var2);

   void unregisterOwner(Plugin var1);

   EliteMindBodyCapabilities bodyCapabilities();

   EliteEntity spawn(EliteMindSpawnRequest var1);

   EliteMindSnapshot setProgram(EliteEntity var1, NamespacedKey var2, EliteMindTransferPolicy var3);

   EliteMindSnapshot setPaused(EliteEntity var1, boolean var2);

   boolean clearProgram(EliteEntity var1);

   Optional<EliteMindSnapshot> inspect(EliteEntity var1);
}
