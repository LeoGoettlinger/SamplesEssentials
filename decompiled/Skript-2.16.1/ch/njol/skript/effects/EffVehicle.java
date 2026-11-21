package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Vehicle")
@Description({"Makes an entity ride another entity, e.g. a minecart, a saddled pig, an arrow, etc."})
@Example.Examples({@Example("make the player ride a saddled pig"), @Example("make the attacker ride the victim")})
@Since({"2.0"})
public class EffVehicle extends Effect {
   @Nullable
   private Expression<Entity> passengers;
   @Nullable
   private Expression<?> vehicles;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.passengers = (Expression<Entity>)(matchedPattern == 2 ? null : exprs[0]);
      this.vehicles = matchedPattern == 1 ? null : exprs[exprs.length - 1];
      return true;
   }

   @Override
   protected void execute(Event event) {
      if (this.vehicles == null) {
         assert this.passengers != null;

         for (Entity passenger : this.passengers.getArray(event)) {
            passenger.leaveVehicle();
         }
      } else if (this.passengers == null) {
         for (Object vehicle : this.vehicles.getArray(event)) {
            ((Entity)vehicle).eject();
         }
      } else {
         Entity[] passengersArray = this.passengers.getArray(event);
         if (passengersArray.length != 0) {
            Object vehicleObject = this.vehicles.getSingle(event);
            if (vehicleObject instanceof Entity vehicleEntity) {
               for (Entity passenger : passengersArray) {
                  assert passenger != null;

                  if (passenger != vehicleEntity) {
                     passenger.leaveVehicle();
                     vehicleEntity.addPassenger(passenger);
                  }
               }
            } else if (vehicleObject instanceof EntityData<?> vehicleData) {
               for (Entity passenger : passengersArray) {
                  assert passenger != null;

                  Entity vehicleEntity = vehicleData.spawn(passenger.getLocation());
                  if (vehicleEntity == null) {
                     return;
                  }

                  passenger.leaveVehicle();
                  vehicleEntity.addPassenger(passenger);
               }
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      if (this.vehicles == null) {
         assert this.passengers != null;

         return "make " + this.passengers.toString(event, debug) + " dismount";
      } else {
         return this.passengers == null
            ? "eject passenger" + (this.vehicles.isSingle() ? "" : "s") + " of " + this.vehicles.toString(event, debug)
            : "make " + this.passengers.toString(event, debug) + " ride " + this.vehicles.toString(event, debug);
      }
   }

   static {
      Skript.registerEffect(
         EffVehicle.class,
         "(make|let|force) %entities% [to] (ride|mount) [(in|on)] %entity/entitydata%",
         "(make|let|force) %entities% [to] (dismount|(dismount|leave) (from|of|) (any|the[ir]|his|her|) vehicle[s])",
         "(eject|dismount) (any|the|) passenger[s] (of|from) %entities%"
      );
   }
}
