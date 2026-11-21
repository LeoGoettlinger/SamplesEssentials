package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Ravager;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class MountDamageHandler {
   public static void handle(EntityDamageByEntityEvent event, RideStateCache state, DemoraLegendaryWeapons plugin) {
      if (event.getDamager() instanceof Ravager ravager) {
         if (state.hasRecentHit(ravager.getUniqueId())) {
            double damage = plugin.getConfig().getDouble("weapons.ravager_horn.auto_attack_damage", event.getDamage());
            event.setDamage(damage);
         }
      }
   }
}
