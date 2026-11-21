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
import io.papermc.paper.entity.Leashable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Leash entities")
@Description({"Leash entities to other entities, or unleash them.", "Most mobs, iron golems, snow golems, and boats can be leashed."})
@Example("on right click:\n\tleash event-entity to player\n\tsend \"&aYou leashed &2%event-entity%!\" to player\n")
@Since({"2.3, 2.16 (all leashable entities)"})
public class EffLeash extends Effect {
   private Expression<Entity> holder;
   private Expression<Entity> targets;
   private boolean leash;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.leash = matchedPattern != 2;
      if (this.leash) {
         this.holder = (Expression<Entity>)exprs[1 - matchedPattern];
         this.targets = (Expression<Entity>)exprs[matchedPattern];
      } else {
         this.targets = (Expression<Entity>)exprs[0];
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      Entity holder = this.leash ? this.holder.getSingle(event) : null;
      if (!this.leash || holder != null) {
         for (Entity target : this.targets.getArray(event)) {
            if (target instanceof Leashable leashable) {
               leashable.setLeashHolder(holder);
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.leash ? "leash " + this.targets.toString(e, debug) + " to " + this.holder.toString(e, debug) : "unleash " + this.targets.toString(e, debug);
   }

   static {
      Skript.registerEffect(
         EffLeash.class, "(leash|lead) %entities% to %entity%", "make %entity% (leash|lead) %entities%", "un(leash|lead) [holder of] %entities%"
      );
   }
}
