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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Tame / Untame")
@Description({"Tame a tameable entity (horse, parrot, cat, etc.)."})
@Example.Examples({@Example("tame {_horse}"), @Example("untame {_horse}")})
@Since({"2.10"})
public class EffTame extends Effect {
   private boolean tame;
   private Expression<Entity> entities;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.tame = !parseResult.hasTag("un");
      this.entities = (Expression<Entity>)exprs[0];
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (Entity entity : this.entities.getArray(event)) {
         if (entity instanceof Tameable) {
            ((Tameable)entity).setTamed(this.tame);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.tame ? "tame " : "untame ") + this.entities.toString(event, debug);
   }

   static {
      Skript.registerEffect(EffTame.class, "[:un](tame|domesticate) %entities%");
   }
}
