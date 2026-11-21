package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Panda Sneeze")
@Description({"Make a panda start/stop sneezing."})
@Example("if last spawned panda is not sneezing:\n\tmake last spawned panda start sneezing\n")
@Since({"2.11"})
public class EffPandaSneezing extends Effect {
   private Expression<LivingEntity> entities;
   private boolean start;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<LivingEntity>)exprs[0];
      this.start = parseResult.hasTag("start");
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (LivingEntity entity : this.entities.getArray(event)) {
         if (entity instanceof Panda panda) {
            panda.setSneezing(this.start);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("make", this.entities);
      if (this.start) {
         builder.append("start");
      } else {
         builder.append("stop");
      }

      builder.append("sneezing");
      return builder.toString();
   }

   static {
      Skript.registerEffect(
         EffPandaSneezing.class, "make %livingentities% (start:(start sneezing|sneeze)|stop sneezing)", "force %livingentities% to (:start|stop) sneezing"
      );
   }
}
