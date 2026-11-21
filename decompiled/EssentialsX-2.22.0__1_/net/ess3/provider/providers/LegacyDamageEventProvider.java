package net.ess3.provider.providers;

import net.ess3.provider.DamageEventProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@ProviderData(
   description = "Legacy Damage Event Provider"
)
public class LegacyDamageEventProvider implements DamageEventProvider {
   @Override
   public EntityDamageEvent callDamageEvent(Player player, DamageCause cause, double damage) {
      EntityDamageEvent ede = new EntityDamageEvent(player, cause, damage);
      player.getServer().getPluginManager().callEvent(ede);
      return ede;
   }
}
