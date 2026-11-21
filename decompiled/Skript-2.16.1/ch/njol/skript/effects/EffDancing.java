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
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dance")
@Description({"Make an allay or piglin start or stop dancing.", "Providing a location only applies to allays. They will check to see if the the block at the location is a jukebox and playing music. If it isn't, they will stop dancing. If no location is provided, the allay will dance indefinitely.", "Providing a timespan only applies for piglins. It determines the length of time they will dance for. If no timespan is provided, they will dance indefinitely."})
@Example.Examples({@Example("if last spawned allay is not dancing:\n\tmake last spawned allay start dancing\n"), @Example("if block at location(0, 0, 0) is a jukebox:\n\tmake all allays dance at location(0, 0, 0)\n"), @Example("make last spawned piglin start dancing"), @Example("make all piglins dance for 5 hours")})
@Since({"2.11"})
public class EffDancing extends Effect {
   private static final boolean SUPPORTS_PIGLINS = Skript.methodExists(Piglin.class, "setDancing", boolean.class);
   private Expression<LivingEntity> entities;
   private boolean start;
   @Nullable
   private Expression<Location> location;
   @Nullable
   private Expression<Timespan> timespan;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<LivingEntity>)exprs[0];
      this.start = matchedPattern == 0;
      if (this.start && exprs[1] != null) {
         this.location = Direction.combine((Expression<? extends Direction>)exprs[1], (Expression<? extends Location>)exprs[2]);
      }

      if (parseResult.hasTag("timespan")) {
         this.timespan = (Expression<Timespan>)exprs[3];
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      Location location = null;
      long time = 0L;
      if (this.location != null) {
         location = this.location.getSingle(event);
      }

      if (this.timespan != null) {
         Timespan timespan1 = this.timespan.getSingle(event);
         if (timespan1 != null) {
            time = timespan1.getAs(Timespan.TimePeriod.TICK);
         }
      }

      for (LivingEntity entity : this.entities.getArray(event)) {
         if (entity instanceof Allay allay) {
            if (!this.start) {
               allay.stopDancing();
            } else if (location != null) {
               allay.startDancing(location);
            } else {
               allay.startDancing();
            }
         } else if (SUPPORTS_PIGLINS && entity instanceof Piglin piglin) {
            if (!this.start) {
               piglin.setDancing(false);
            } else if (time > 0L) {
               piglin.setDancing(time);
            } else {
               piglin.setDancing(true);
            }
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

      builder.append("dancing");
      if (this.location != null) {
         builder.append(this.location);
      }

      if (this.timespan != null) {
         builder.append("for", this.timespan);
      }

      return builder.toString();
   }

   static {
      Skript.registerEffect(
         EffDancing.class,
         "make %livingentities% (start dancing|dance) [%-direction% %-location%] [timespan:for %-timespan%]",
         "make %livingentities% (stop dancing|not dance)"
      );
   }
}
