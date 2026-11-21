package com.magmaguy.elitemobs.advancedcombat.input;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityResult;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import org.bukkit.entity.Player;

public interface ClassAbilityInput {
   boolean hasActiveClass(Player var1);

   boolean fLayerSupported(Player var1);

   boolean controlsAlwaysAvailable(Player var1);

   boolean outsideControlsAllowed();

   void onControlModeChanged(Player var1);

   String abilityName(Player var1, AbilitySlot var2);

   AbilityResult useAbility(Player var1, AbilitySlot var2);
}
