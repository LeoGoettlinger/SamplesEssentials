package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import java.util.function.Predicate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.Nullable;

@Name("Vehicle")
@Description({"The vehicle an entity is in, if any.", "This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.", "See also: <a href='#ExprPassenger'>passenger</a>"})
@Example.Examples({@Example("set the vehicle of {game::players::*} to a saddled pig\ngive {game::players::*} a carrot on a stick\n"), @Example("on vehicle enter:\n\tvehicle is a horse\n\tadd 1 to {statistics::horseMounting::%uuid of player%}\n")})
@Since({"2.0"})
public class ExprVehicle extends PropertyExpression<Entity, Entity> {
   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Entity>)expressions[0]);
      return true;
   }

   protected Entity[] get(Event event, Entity[] source) {
      if (event instanceof EntityDismountEvent entityDismountEvent && this.getTime() != EventValues.TIME_FUTURE) {
         return this.get(source, e -> e.equals(entityDismountEvent.getEntity()) ? entityDismountEvent.getDismounted() : e.getVehicle());
      } else if (event instanceof VehicleEnterEvent vehicleEnterEvent && this.getTime() != EventValues.TIME_PAST) {
         return this.get(source, e -> (Entity)(e.equals(vehicleEnterEvent.getEntered()) ? vehicleEnterEvent.getVehicle() : e.getVehicle()));
      } else if (event instanceof VehicleExitEvent vehicleExitEvent && this.getTime() != EventValues.TIME_FUTURE) {
         return this.get(source, e -> (Entity)(e.equals(vehicleExitEvent.getExited()) ? vehicleExitEvent.getVehicle() : e.getVehicle()));
      } else {
         return event instanceof EntityMountEvent entityMountEvent && this.getTime() != EventValues.TIME_PAST
            ? this.get(source, e -> e.equals(entityMountEvent.getEntity()) ? entityMountEvent.getMount() : e.getVehicle())
            : this.get(source, Entity::getVehicle);
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.SET) {
         if (this.isDefault() && this.getParser().isCurrentEvent(VehicleExitEvent.class, EntityDismountEvent.class)) {
            Skript.error("Setting the vehicle during a dismount/exit vehicle event will create an infinite mounting loop.");
            return null;
         } else {
            return new Class[]{Entity.class, EntityData.class};
         }
      } else {
         return super.acceptChange(mode);
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.SET) {
         Predicate<Entity> predicate = Player.class::isInstance;
         if (event instanceof EntityMountEvent entityMountEvent && predicate.test(entityMountEvent.getEntity())) {
            return;
         }

         if (event instanceof VehicleEnterEvent vehicleEnterEvent && predicate.test(vehicleEnterEvent.getEntered())) {
            return;
         }

         Entity[] passengers = this.getExpr().getArray(event);
         if (passengers.length == 0) {
            return;
         }

         assert delta != null;

         Object object = delta[0];
         if (object instanceof Entity entity) {
            entity.eject();

            for (Entity passenger : passengers) {
               if ((!(event instanceof VehicleExitEvent) || !predicate.test(passenger) || !passenger.equals(((VehicleExitEvent)event).getExited()))
                  && (!(event instanceof EntityDismountEvent) || !predicate.test(passenger) || !passenger.equals(((EntityDismountEvent)event).getEntity()))) {
                  assert passenger != null;

                  passenger.leaveVehicle();
                  entity.addPassenger(passenger);
               }
            }
         } else if (object instanceof EntityData entityData) {
            VehicleExitEvent vehicleExitEvent = event instanceof VehicleExitEvent ? (VehicleExitEvent)event : null;
            EntityDismountEvent entityDismountEvent = event instanceof EntityDismountEvent ? (EntityDismountEvent)event : null;

            for (Entity passengerx : passengers) {
               if ((vehicleExitEvent == null || !predicate.test(passengerx) || !passengerx.equals(vehicleExitEvent.getExited()))
                  && (entityDismountEvent == null || !predicate.test(passengerx) || !passengerx.equals(entityDismountEvent.getEntity()))) {
                  Entity vehicle = entityData.spawn(passengerx.getLocation());
                  if (vehicle != null) {
                     vehicle.addPassenger(passengerx);
                  }
               }
            }
         } else {
            assert false;
         }
      } else {
         super.change(event, delta, mode);
      }
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(
         time, this.getExpr(), new Class[]{VehicleEnterEvent.class, VehicleExitEvent.class, EntityMountEvent.class, EntityDismountEvent.class}
      );
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "vehicle of " + this.getExpr().toString(event, debug);
   }

   static {
      if (Skript.classExists("org.bukkit.event.entity.EntityMountEvent")) {
         registerDefault(ExprVehicle.class, Entity.class, "vehicle[s]", "entities");
      }
   }
}
