package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Lightning")
@Description({"Strike lightning at a given location. Can use 'lightning effect' to create a lightning that does not harm entities or start fires."})
@Example.Examples({@Example("strike lightning at the player"), @Example("strike lightning effect at the victim")})
@Since({"1.4"})
public class EffLightning extends Effect {
   private Expression<Location> locations;
   private boolean effectOnly;
   @Nullable
   public static Entity lastSpawned = null;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.locations = Direction.combine((Expression<? extends Direction>)exprs[0], (Expression<? extends Location>)exprs[1]);
      this.effectOnly = parseResult.mark == 1;
      return true;
   }

   @Override
   protected void execute(Event e) {
      for (Location l : this.locations.getArray(e)) {
         if (this.effectOnly) {
            lastSpawned = l.getWorld().strikeLightningEffect(l);
         } else {
            lastSpawned = l.getWorld().strikeLightning(l);
         }
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "strike lightning " + (this.effectOnly ? "effect " : "") + this.locations.toString(e, debug);
   }

   static {
      Skript.registerEffect(EffLightning.class, "(create|strike) lightning(1¦[ ]effect|) %directions% %locations%");
   }
}
