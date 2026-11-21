package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.jetbrains.annotations.Nullable;

@Name("Make Incendiary")
@Description({"Sets if an entity's explosion will leave behind fire. This effect is also usable in an explosion prime event."})
@Example("on explosion prime:\n\tmake the explosion fiery\n")
@Since({"2.5"})
public class EffIncendiary extends Effect {
   private Expression<Entity> entities;
   private boolean causeFire;
   private boolean isEvent;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isEvent = matchedPattern == 2;
      if (this.isEvent && !this.getParser().isCurrentEvent(ExplosionPrimeEvent.class)) {
         Skript.error("Making 'the explosion' fiery is only usable in an explosion prime event", ErrorQuality.SEMANTIC_ERROR);
         return false;
      } else {
         if (!this.isEvent) {
            this.entities = (Expression<Entity>)exprs[0];
         }

         this.causeFire = parseResult.mark != 1;
         return true;
      }
   }

   @Override
   protected void execute(Event e) {
      if (this.isEvent) {
         if (!(e instanceof ExplosionPrimeEvent)) {
            return;
         }

         ((ExplosionPrimeEvent)e).setFire(this.causeFire);
      } else {
         for (Entity entity : this.entities.getArray(e)) {
            if (entity instanceof Explosive) {
               ((Explosive)entity).setIsIncendiary(this.causeFire);
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.isEvent
         ? "make the event-explosion " + (this.causeFire ? "" : "not") + " fiery"
         : "make " + this.entities.toString(e, debug) + (this.causeFire ? "" : " not") + " incendiary";
   }

   static {
      Skript.registerEffect(
         EffIncendiary.class,
         "make %entities% [(1¦not)] incendiary",
         "make %entities%'[s] explosion [(1¦not)] (incendiary|fiery)",
         "make [the] [event(-| )]explosion [(1¦not)] (incendiary|fiery)"
      );
   }
}
