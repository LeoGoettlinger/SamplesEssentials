package com.oheers.fish.api.boost;

import com.oheers.fish.api.registry.RegistryItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public interface RarityWeightBoost extends RegistryItem {
   double weightMultiplier(@NonNull Player var1, @NonNull Location var2, @NonNull String var3);
}
