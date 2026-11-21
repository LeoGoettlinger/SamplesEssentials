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
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Force Eating")
@Description({"Make a panda or horse type (horse, camel, donkey, llama, mule) start/stop eating."})
@Example("if last spawned panda is eating:\n\tmake last spawned panda stop eating\n")
@Since({"2.11"})
public class EffEating extends Effect {
   private static final boolean SUPPORTS_HORSES = Skript.methodExists(AbstractHorse.class, "isEating");
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
            panda.setEating(this.start);
         } else if (SUPPORTS_HORSES && entity instanceof AbstractHorse horse) {
            horse.setEating(this.start);
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

      builder.append("eating");
      return builder.toString();
   }

   static {
      Skript.registerEffect(EffEating.class, "make %livingentities% (:start|stop) eating", "force %livingentities% to (:start|stop) eating");
   }
}
