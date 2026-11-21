package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PassengerUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

@Name("Passenger")
@Description({"The passenger of a vehicle, or the rider of a mob.", "For 1.11.2 and above, it returns a list of passengers and you can use all changers in it.", "See also: <a href='#ExprVehicle'>vehicle</a>"})
@Example("passengers of the minecart contains a creeper or a cow\nthe boat's passenger contains a pig\nadd a cow and a zombie to passengers of last spawned boat\nset passengers of player's vehicle to a pig and a horse\nremove all pigs from player's vehicle\nclear passengers of boat\n")
@Since({"2.0, 2.2-dev26 (Multiple passengers for 1.11.2+)"})
public class ExprPassenger extends SimpleExpression<Entity> {
   private Expression<Entity> vehicle;

   @Nullable
   protected Entity[] get(final Event e) {
      Entity[] source = this.vehicle.getAll(e);
      Converter<Entity, Entity[]> conv = new Converter<Entity, Entity[]>() {
         @Nullable
         public Entity[] convert(Entity v) {
            if (ExprPassenger.this.getTime() >= 0 && e instanceof VehicleEnterEvent && v.equals(((VehicleEnterEvent)e).getVehicle()) && !Delay.isDelayed(e)) {
               return new Entity[]{((VehicleEnterEvent)e).getEntered()};
            } else {
               return ExprPassenger.this.getTime() >= 0 && e instanceof VehicleExitEvent && v.equals(((VehicleExitEvent)e).getVehicle()) && !Delay.isDelayed(e)
                  ? new Entity[]{((VehicleExitEvent)e).getExited()}
                  : PassengerUtils.getPassenger(v);
            }
         }
      };
      List<Entity> entities = new ArrayList<>();

      for (Entity v : source) {
         if (v != null) {
            Entity[] array = conv.convert(v);
            if (array != null && array.length > 0) {
               entities.addAll(Arrays.asList(array));
            }
         }
      }

      return entities.toArray(new Entity[entities.size()]);
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.vehicle = (Expression<Entity>)exprs[0];
      return true;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.isSingle()) {
         return new Class[]{Entity[].class, EntityData[].class};
      } else {
         return mode == Changer.ChangeMode.SET ? new Class[]{Entity.class, EntityData.class} : super.acceptChange(mode);
      }
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Entity[] vehicles = this.vehicle.getArray(e);
      if (this.isSingle() && mode != Changer.ChangeMode.SET) {
         super.change(e, delta, mode);
      } else {
         for (Entity vehicle : vehicles) {
            if (vehicle != null) {
               switch (mode) {
                  case SET:
                     vehicle.eject();
                  case ADD:
                     if (delta == null || delta.length == 0) {
                        return;
                     }

                     for (Object obj : delta) {
                        if (obj != null) {
                           Entity passengerx = obj instanceof Entity ? (Entity)obj : ((EntityData)obj).spawn(vehicle.getLocation());
                           PassengerUtils.addPassenger(vehicle, passengerx);
                        }
                     }
                     break;
                  case REMOVE_ALL:
                  case REMOVE:
                     if (delta == null || delta.length == 0) {
                        return;
                     }

                     Object[] var9 = delta;
                     int var10 = delta.length;
                     int var11 = 0;

                     for (; var11 < var10; var11++) {
                        Object obj = var9[var11];
                        if (obj != null) {
                           if (obj instanceof Entity) {
                              PassengerUtils.removePassenger(vehicle, (Entity)obj);
                           } else {
                              for (Entity passenger : PassengerUtils.getPassenger(vehicle)) {
                                 if (passenger != null && ((EntityData)obj).isInstance(passenger)) {
                                    PassengerUtils.removePassenger(vehicle, passenger);
                                 }
                              }
                           }
                        }
                     }
                     break;
                  case RESET:
                  case DELETE:
                     vehicle.eject();
               }
            }
         }
      }
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the passenger of " + this.vehicle.toString(e, debug);
   }

   @Override
   public boolean isSingle() {
      return !PassengerUtils.hasMultiplePassenger() ? this.vehicle.isSingle() : false;
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(time, this.vehicle, VehicleEnterEvent.class, VehicleExitEvent.class);
   }

   static {
      Skript.registerExpression(ExprPassenger.class, Entity.class, ExpressionType.PROPERTY, "[the] passenger[s] of %entities%", "%entities%'[s] passenger[s]");
   }
}
