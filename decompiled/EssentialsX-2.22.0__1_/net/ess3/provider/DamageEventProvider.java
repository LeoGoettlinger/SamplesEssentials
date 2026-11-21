package net.ess3.provider;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public interface DamageEventProvider extends Provider {
   EntityDamageEvent callDamageEvent(Player player, DamageCause cause, double damage);
}
