package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface ScriptExecutable {
   void check(EliteEntity var1, LivingEntity var2, ScriptActionData var3);

   void check(Location var1, ScriptActionData var2);
}
