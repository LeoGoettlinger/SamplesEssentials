package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface TargetDebuffBonus {
   boolean appliesTo(LivingEntity var1, Player var2);

   SkillType levelSource();

   double bonusFor(Player var1, LivingEntity var2, int var3);

   String debugLabel();
}
