package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Optional;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface MindPerception {
   Optional<LivingEntity> currentTarget();

   Optional<Player> nearestPlayer(double var1);

   boolean lineOfSight(LivingEntity var1);

   default MindNavigationStatus navigation() {
      return MindNavigationStatus.idle();
   }
}
