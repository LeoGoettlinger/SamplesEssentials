package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import org.bukkit.entity.Player;

public interface ClassAbilityEngine extends AutoCloseable {
   AbilityResult execute(Player var1, ClassLineage var2, AbilitySlot var3, int var4);

   void deactivate(Player var1);

   @Override
   void close();
}
