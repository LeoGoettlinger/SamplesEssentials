package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Toggle Picking Up Items")
@Description({"Determines whether living entities are able to pick up items or not"})
@Example.Examples({@Example("forbid player from picking up items"), @Example("send \"You can no longer pick up items!\" to player"), @Example("on drop:\n\tif player can't pick up items:\n\t\tallow player to pick up items\n")})
@Since({"2.8.0"})
public class EffToggleCanPickUpItems extends Effect {
   private Expression<LivingEntity> entities;
   private boolean allowPickUp;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<LivingEntity>)exprs[0];
      this.allowPickUp = matchedPattern == 0;
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (LivingEntity entity : this.entities.getArray(event)) {
         entity.setCanPickupItems(this.allowPickUp);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.allowPickUp
         ? "allow " + this.entities.toString(event, debug) + " to pick up items"
         : "forbid " + this.entities.toString(event, debug) + " from picking up items";
   }

   static {
      Skript.registerEffect(
         EffToggleCanPickUpItems.class,
         "allow %livingentities% to pick([ ]up items| items up)",
         "(forbid|disallow) %livingentities% (from|to) pick([ing | ]up items|[ing] items up)"
      );
   }
}
