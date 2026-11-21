package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.jetbrains.annotations.Nullable;

@Name("Attacker")
@Description({"The attacker of a damage event, e.g. when a player attacks a zombie this expression represents the player.\",\nPlease note that the attacker can also be a block, e.g. a cactus or lava, but this expression will not be set in these cases.\n"})
@Example("on damage:\n\tattacker is a player\n\thealth of attacker is less than or equal to 2\n\tdamage victim by 1 heart\n")
@Since({"1.3"})
@Events({"damage", "death", "vehicle destroy", "attempt attack"})
public class ExprAttacker extends SimpleExpression<Entity> implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(
         EntityDamageEvent.class, EntityDeathEvent.class, VehicleDamageEvent.class, VehicleDestroyEvent.class, PrePlayerAttackEntityEvent.class
      );
   }

   protected Entity[] get(Event e) {
      return new Entity[]{getAttacker(e)};
   }

   @Nullable
   static Entity getAttacker(@Nullable Event event) {
      if (event == null) {
         return null;
      } else if (event instanceof EntityDamageByEntityEvent damageEvent) {
         Entity damager = damageEvent.getDamager();
         if (damager instanceof Projectile projectile) {
            return projectile.getShooter() instanceof Entity shooterEntity ? shooterEntity : null;
         } else {
            return damager;
         }
      } else if (event instanceof EntityDeathEvent deathEvent) {
         return getAttacker(deathEvent.getEntity().getLastDamageCause());
      } else if (event instanceof VehicleDamageEvent vehicleDamageEvent) {
         return vehicleDamageEvent.getAttacker();
      } else if (event instanceof VehicleDestroyEvent vehicleDestroyEvent) {
         return vehicleDestroyEvent.getAttacker();
      } else {
         return event instanceof PrePlayerAttackEntityEvent preAttackEvent ? preAttackEvent.getPlayer() : null;
      }
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return e == null ? "the attacker" : Classes.getDebugMessage(this.getSingle(e));
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   static {
      Skript.registerExpression(ExprAttacker.class, Entity.class, ExpressionType.SIMPLE, "[the] (attacker|damager)");
   }
}
