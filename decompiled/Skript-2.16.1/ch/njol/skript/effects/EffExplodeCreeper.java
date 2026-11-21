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
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Explode Creeper")
@Description({"Starts the explosion process of a creeper or instantly explodes it."})
@Example.Examples({@Example("start explosion of the last spawned creeper"), @Example("stop ignition of the last spawned creeper")})
@Since({"2.5"})
public class EffExplodeCreeper extends Effect {
   private Expression<LivingEntity> entities;
   private boolean instant;
   private boolean stop;
   private final boolean paper = Skript.methodExists(Creeper.class, "setIgnited", boolean.class);

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 4 && !this.paper) {
         Skript.error("Stopping the ignition process is only possible on Paper 1.13+", ErrorQuality.SEMANTIC_ERROR);
         return false;
      } else {
         this.entities = (Expression<LivingEntity>)exprs[0];
         this.instant = matchedPattern == 0;
         this.stop = matchedPattern == 4;
         return true;
      }
   }

   @Override
   protected void execute(Event e) {
      for (LivingEntity le : this.entities.getArray(e)) {
         if (le instanceof Creeper) {
            if (this.instant) {
               ((Creeper)le).explode();
            } else if (this.stop) {
               ((Creeper)le).setIgnited(false);
            } else if (this.paper) {
               ((Creeper)le).setIgnited(true);
            } else {
               ((Creeper)le).ignite();
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return (this.instant ? "instantly explode " : "start the explosion process of ") + this.entities.toString(e, debug);
   }

   static {
      if (Skript.methodExists(Creeper.class, "explode")) {
         Skript.registerEffect(
            EffExplodeCreeper.class,
            "instantly explode [creeper[s]] %livingentities%",
            "explode [creeper[s]] %livingentities% instantly",
            "ignite creeper[s] %livingentities%",
            "start (ignition|explosion) [process] of [creeper[s]] %livingentities%",
            "stop (ignition|explosion) [process] of [creeper[s]] %livingentities%"
         );
      }
   }
}
