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
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Nullable;

@Name("Launch firework")
@Description({"Launch firework effects at the given location(s)."})
@Example("launch ball large colored red, purple and white fading to light green and black at player's location with duration 1")
@Since({"2.4"})
public class EffFireworkLaunch extends Effect {
   @Nullable
   public static Entity lastSpawned = null;
   private Expression<FireworkEffect> effects;
   private Expression<Location> locations;
   private Expression<Number> lifetime;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.effects = (Expression<FireworkEffect>)exprs[0];
      this.locations = (Expression<Location>)exprs[1];
      this.lifetime = (Expression<Number>)exprs[2];
      return true;
   }

   @Override
   protected void execute(Event event) {
      FireworkEffect[] effects = this.effects.getArray(event);
      int power = this.lifetime.getOptionalSingle(event).orElse(1).intValue();
      power = Math.min(127, Math.max(0, power));

      for (Location location : this.locations.getArray(event)) {
         World world = location.getWorld();
         if (world != null) {
            Firework firework = (Firework)world.spawn(location, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffects(effects);
            meta.setPower(power);
            firework.setFireworkMeta(meta);
            lastSpawned = firework;
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "Launch firework(s) "
         + this.effects.toString(event, debug)
         + " at location(s) "
         + this.locations.toString(event, debug)
         + " timed "
         + this.lifetime.toString(event, debug);
   }

   static {
      Skript.registerEffect(
         EffFireworkLaunch.class, "(launch|deploy) [[a] firework [with effect[s]]] %fireworkeffects% at %locations% [([with] (duration|power)|timed) %number%]"
      );
   }
}
