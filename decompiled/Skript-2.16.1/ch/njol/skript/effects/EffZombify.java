package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import java.util.function.Function;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Zombify Villager")
@Description({"Turn a villager into a zombie villager. Cure a zombie villager immediately or after specified amount of time.", "This effect removes the old entity and creates a new entity.", "Zombifying a villager stored in a variable will update the variable to the new zombie villager.", "Curing a zombie villager does not update the variable."})
@Example.Examples({@Example("zombify last spawned villager"), @Example("set {_villager} to last spawned villager\nzombify {_villager}\nif {_villager} is a zombie villager:\n\t# This will pass because '{_villager}' gets changed to the new zombie villager\n"), @Example("set {_villager} to last spawned villager\nzombify last spawned villager\nif {_villager} is a zombie villager:\n\t# This will fail because the variable was not provided when zombifying\n"), @Example("unzombify {_zombieVillager}"), @Example("unzombify {_zombieVillager} after 2 seconds")})
@Since({"2.11"})
public class EffZombify extends Effect {
   private Expression<LivingEntity> entities;
   @Nullable
   private Expression<Timespan> timespan;
   private boolean zombify;
   private boolean changeInPlace = false;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<LivingEntity>)exprs[0];
      this.zombify = matchedPattern == 0;
      if (!this.zombify && exprs[1] != null) {
         this.timespan = (Expression<Timespan>)exprs[1];
      }

      if (Changer.ChangerUtils.acceptsChange(this.entities, Changer.ChangeMode.SET, LivingEntity.class)) {
         this.changeInPlace = true;
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      int ticks = 0;
      if (this.timespan != null) {
         Timespan timespan = this.timespan.getSingle(event);
         if (timespan != null) {
            ticks = (int)timespan.getAs(Timespan.TimePeriod.TICK);
         }
      }

      int finalTicks = ticks;
      Function<LivingEntity, LivingEntity> changeFunction = entityx -> {
         if (this.zombify && entityx instanceof Villager villager) {
            return villager.zombify();
         } else {
            if (!this.zombify && entityx instanceof ZombieVillager zombieVillager) {
               zombieVillager.setConversionTime(finalTicks);
            }

            return entityx;
         }
      };
      if (this.changeInPlace) {
         this.entities.changeInPlace(event, changeFunction);
      } else {
         for (LivingEntity entity : this.entities.getAll(event)) {
            changeFunction.apply(entity);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      if (this.zombify) {
         builder.append("zombify");
      } else {
         builder.append("unzombify");
      }

      builder.append(this.entities);
      if (this.timespan != null) {
         builder.append("after", this.timespan);
      }

      return builder.toString();
   }

   static {
      Skript.registerEffect(EffZombify.class, "zombify %livingentities%", "unzombify %livingentities% [(in|after) %-timespan%]");
   }
}
