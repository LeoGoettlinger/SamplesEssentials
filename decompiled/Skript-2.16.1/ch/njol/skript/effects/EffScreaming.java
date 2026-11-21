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
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Entity Scream")
@Description({"Make a goat or enderman start or stop screaming."})
@Example.Examples({@Example("\tmake last spawned goat start screaming\n\tforce last spawned goat to stop screaming\n"), @Example("\tmake {_enderman} scream\n\tforce {_enderman} to stop screaming\n")})
@Since({"2.11"})
public class EffScreaming extends Effect {
   private static final boolean SUPPORTS_ENDERMAN = Skript.methodExists(Enderman.class, "setScreaming", boolean.class);
   private Expression<LivingEntity> entities;
   private boolean scream;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<LivingEntity>)exprs[0];
      this.scream = matchedPattern <= 1;
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (LivingEntity entity : this.entities.getArray(event)) {
         if (entity instanceof Goat goat) {
            goat.setScreaming(this.scream);
         } else if (SUPPORTS_ENDERMAN && entity instanceof Enderman enderman) {
            enderman.setScreaming(this.scream);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "make " + this.entities.toString(event, debug) + (this.scream ? " start " : " stop ") + "screaming";
   }

   static {
      Skript.registerEffect(
         EffScreaming.class,
         "make %livingentities% (start screaming|scream)",
         "force %livingentities% to (start screaming|scream)",
         "make %livingentities% stop screaming",
         "force %livingentities% to stop screaming"
      );
   }
}
