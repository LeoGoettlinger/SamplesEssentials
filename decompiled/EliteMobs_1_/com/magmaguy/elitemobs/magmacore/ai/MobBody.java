package com.magmaguy.elitemobs.magmacore.ai;

import org.bukkit.entity.LivingEntity;

public interface MobBody {
   LivingEntity entity();

   MindBodyProfile profile();

   MindBodyCapabilities capabilities();

   boolean isValid();
}
