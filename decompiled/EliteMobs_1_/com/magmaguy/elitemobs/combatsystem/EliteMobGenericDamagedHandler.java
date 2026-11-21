package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

public class EliteMobGenericDamagedHandler implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void eliteMobDamagedGeneric(EliteMobDamagedEvent event) {
      if (!event.getEntityDamageEvent().getCause().equals(DamageCause.ENTITY_ATTACK)) {
         if (!event.getEntityDamageEvent().getCause().equals(DamageCause.CUSTOM)) {
            event.getEntityDamageEvent().setDamage(DamageModifier.BASE, event.getEntityDamageEvent().getDamage());
         }
      }
   }
}
