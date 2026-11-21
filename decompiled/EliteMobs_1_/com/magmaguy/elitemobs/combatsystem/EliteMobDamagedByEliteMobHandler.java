package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.EliteMobDamagedByEliteMobEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

public class EliteMobDamagedByEliteMobHandler implements Listener {
   @EventHandler(
      ignoreCancelled = true
   )
   public void onIronGolemDamage(EliteMobDamagedByEliteMobEvent event) {
      if (event.getDamager().getLivingEntity() != null && event.getDamagee().getLivingEntity() != null) {
         if (event.getDamager().getLivingEntity().getType().equals(EntityType.IRON_GOLEM)
            || event.getDamagee().getLivingEntity().getType().equals(EntityType.IRON_GOLEM)) {
            double damage = event.getEntityDamageByEntityEvent().getDamage() * event.getDamager().getLevel() * 0.5;

            for (DamageModifier modifier : DamageModifier.values()) {
               if (event.getEntityDamageByEntityEvent().isApplicable(modifier)) {
                  event.getEntityDamageByEntityEvent().setDamage(modifier, 0.0);
               }
            }

            event.setDamage(damage);
         }
      }
   }
}
